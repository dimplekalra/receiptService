package com.maven.receipts.service.impl;

import com.maven.receipts.dto.TransactionResponse;
import com.maven.receipts.entity.ExpenseTransaction;
import com.maven.receipts.entity.LineItem;
import com.maven.receipts.entity.Receipt;
import com.maven.receipts.entity.Tax;
import com.maven.receipts.enums.ItemizeStatus;
import com.maven.receipts.exception.ReceiptNotFoundException;
import com.maven.receipts.mapper.TransactionMapper;
import com.maven.receipts.ocr.OcrService;
import com.maven.receipts.processing.ParsedReceipt;
import com.maven.receipts.processing.ReceiptParser;
import com.maven.receipts.repository.ExpenseTransactionRepository;
import com.maven.receipts.repository.ReceiptRepository;
import com.maven.receipts.service.MoneyReconciliationService;
import com.maven.receipts.service.ReceiptProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReceiptProcessingServiceImpl implements ReceiptProcessingService {

        private final ReceiptRepository receiptRepository;
        private final ExpenseTransactionRepository transactionRepository;
        private final OcrService ocrService;
        private final ReceiptParser receiptParser;
        private final TransactionMapper transactionMapper;
        private final MoneyReconciliationService reconciliationService;

        /*
         * Serializes processing of the same receipt inside this application
         * instance.
         *
         * The database UNIQUE(receipt_id) constraint remains the final
         * protection against duplicate transactions.
         */
        private final java.util.concurrent.ConcurrentHashMap<Long, Object> locks = new java.util.concurrent.ConcurrentHashMap<>();

        @Override
        public TransactionResponse process(Long receiptId) {
                Object lock = locks.computeIfAbsent(
                                receiptId,
                                ignored -> new Object());

                synchronized (lock) {
                        try {
                                return processTransaction(receiptId);
                        } finally {
                                locks.remove(receiptId, lock);
                        }
                }
        }

        @Transactional
        protected TransactionResponse processTransaction(Long receiptId) {

                Receipt receipt = receiptRepository.findById(receiptId)
                                .orElseThrow(() -> new ReceiptNotFoundException(receiptId));

                String ocrText = ocrService.extractText(receipt);

                ParsedReceipt parsed = receiptParser.parse(ocrText);

                validateParsedReceipt(parsed);

                /*
                 * Persist OCR even when itemization needs review.
                 */
                receipt.setOcrText(ocrText);
                receipt.setProcessed(true);

                ExpenseTransaction transaction = transactionRepository.findByReceiptId(receiptId)
                                .orElseGet(() -> ExpenseTransaction.builder()
                                                .receipt(receipt)
                                                .build());

                /*
                 * Header fields are replaced on every processing run.
                 */
                transaction.setMerchant(parsed.merchant());
                transaction.setTransactionDate(parsed.date());
                transaction.setCurrency(parsed.currency());
                transaction.setGrandTotal(parsed.grandTotal());

                /*
                 * Re-processing must not append duplicate taxes/items.
                 * Replace the previous children.
                 */
                List<Tax> taxes = parsed.taxes()
                                .stream()
                                .map(parsedTax -> Tax.builder()
                                                .name(parsedTax.name())
                                                .rate(parsedTax.rate())
                                                .amount(parsedTax.amount())
                                                .build())
                                .toList();

                transaction.replaceTaxes(taxes);

                List<LineItem> items = parsed.lineItems()
                                .stream()
                                .map(parsedItem -> LineItem.builder()
                                                .description(parsedItem.description())
                                                .amount(parsedItem.amount())
                                                .build())
                                .toList();

                transaction.replaceLineItems(items);

                MoneyReconciliationService.ReconciliationResult result = reconciliationService.reconcile(
                                transaction,
                                items);

                transaction.setItemizeStatus(
                                result.matches()
                                                ? ItemizeStatus.COMPLETE
                                                : ItemizeStatus.NEEDS_REVIEW);

                ExpenseTransaction saved = transactionRepository.save(transaction);

                /*
                 * Flush while still inside the transaction. This means any DB
                 * constraint violation occurs before we return.
                 */
                transactionRepository.flush();

                return transactionMapper.toResponse(saved);
        }

        private void validateParsedReceipt(
                        ParsedReceipt parsed) {

                if (parsed.merchant() == null ||
                                parsed.merchant().isBlank()) {

                        throw new IllegalArgumentException(
                                        "Merchant could not be extracted.");
                }

                if (parsed.date() == null) {

                        throw new IllegalArgumentException(
                                        "Transaction date could not be extracted.");
                }

                if (parsed.currency() == null ||
                                parsed.currency().isBlank()) {

                        throw new IllegalArgumentException(
                                        "Currency could not be extracted.");
                }

                if (parsed.grandTotal() == null) {

                        throw new IllegalArgumentException(
                                        "Grand total could not be extracted.");
                }
        }
}
