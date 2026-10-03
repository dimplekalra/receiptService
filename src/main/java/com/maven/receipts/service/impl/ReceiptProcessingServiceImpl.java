package com.maven.receipts.service.impl;

import com.maven.receipts.dto.TransactionResponse;
import com.maven.receipts.entity.ExpenseTransaction;
import com.maven.receipts.entity.LineItem;
import com.maven.receipts.entity.Receipt;
import com.maven.receipts.entity.Tax;
import com.maven.receipts.enums.ItemizeStatus;
import com.maven.receipts.exception.ReceiptNotFoundException;
import com.maven.receipts.ocr.OcrService;
import com.maven.receipts.processing.ParsedReceipt;
import com.maven.receipts.processing.ReceiptParser;
import com.maven.receipts.repository.ExpenseTransactionRepository;
import com.maven.receipts.repository.ReceiptRepository;
import com.maven.receipts.service.ReceiptProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReceiptProcessingServiceImpl implements ReceiptProcessingService {

    private final ReceiptRepository receiptRepository;
    private final ExpenseTransactionRepository transactionRepository;
    private final OcrService ocrService;
    private final ReceiptParser receiptParser;

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
        transaction.getTaxes().clear();
        transaction.getLineItems().clear();

        for (ParsedReceipt.ParsedTax parsedTax : parsed.taxes()) {
            Tax tax = Tax.builder()
                    .name(parsedTax.name())
                    .rate(parsedTax.rate())
                    .amount(parsedTax.amount())
                    .build();

            transaction.addTax(tax);
        }

        for (ParsedReceipt.ParsedLineItem parsedItem : parsed.lineItems()) {
            LineItem item = LineItem.builder()
                    .description(parsedItem.description())
                    .amount(parsedItem.amount())
                    .build();

            transaction.addLineItem(item);
        }

        transaction.setItemizeStatus(
                calculateItemizeStatus(transaction));

        ExpenseTransaction saved = transactionRepository.save(transaction);

        /*
         * Flush while still inside the transaction. This means any DB
         * constraint violation occurs before we return.
         */
        transactionRepository.flush();

        return toResponse(saved);
    }

    private ItemizeStatus calculateItemizeStatus(
            ExpenseTransaction transaction) {
        /*
         * Assignment reconciliation rule:
         *
         * line items + stored taxes == grand total
         */
        BigDecimal itemTotal = transaction.getLineItems()
                .stream()
                .map(LineItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal taxTotal = transaction.getTaxes()
                .stream()
                .map(Tax::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal calculatedTotal = itemTotal.add(taxTotal);

        if (calculatedTotal.compareTo(
                transaction.getGrandTotal()) == 0
                && !transaction.getLineItems().isEmpty()) {

            return ItemizeStatus.COMPLETE;
        }

        return ItemizeStatus.NEEDS_REVIEW;
    }

    private TransactionResponse toResponse(
            ExpenseTransaction transaction) {
        List<TransactionResponse.TaxResponse> taxes = transaction.getTaxes()
                .stream()
                .map(tax -> new TransactionResponse.TaxResponse(
                        tax.getId(),
                        tax.getName(),
                        tax.getRate(),
                        tax.getAmount()))
                .toList();

        List<TransactionResponse.LineItemResponse> items = transaction.getLineItems()
                .stream()
                .map(item -> new TransactionResponse.LineItemResponse(
                        item.getId(),
                        item.getDescription(),
                        item.getAmount(),
                        item.getQuantity(),
                        item.getTaxAmount()))
                .toList();

        return new TransactionResponse(
                transaction.getId(),
                transaction.getReceipt().getId(),
                transaction.getMerchant(),
                transaction.getTransactionDate(),
                transaction.getCurrency(),
                transaction.getGrandTotal(),
                transaction.getItemizeStatus(),
                taxes,
                items);
    }
}
