package com.maven.receipts.service.impl;

import com.maven.receipts.dto.TransactionResponse;
import com.maven.receipts.entity.ExpenseTransaction;
import com.maven.receipts.entity.LineItem;
import com.maven.receipts.entity.Receipt;
import com.maven.receipts.enums.ItemizeStatus;
import com.maven.receipts.exception.TransactionNotFoundException;
import com.maven.receipts.mapper.TransactionMapper;
import com.maven.receipts.processing.ParsedReceipt;
import com.maven.receipts.processing.ReceiptParser;
import com.maven.receipts.repository.ExpenseTransactionRepository;
import com.maven.receipts.service.MoneyReconciliationService;
import com.maven.receipts.service.TransactionItemizeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionItemizeServiceImpl implements TransactionItemizeService {
    private final ExpenseTransactionRepository repository;
    private final ReceiptParser parser;
    private final TransactionMapper mapper;
    private final MoneyReconciliationService reconciliationService;

    @Override
    @Transactional
    public TransactionResponse reItemize(Long transactionId) {

        ExpenseTransaction transaction = repository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException(transactionId));

        Receipt receipt = transaction.getReceipt();

        String ocr = receipt.getOcrText();

        ParsedReceipt parsed = parser.parse(ocr);

        List<LineItem> items = parsed.lineItems()
                .stream()
                .map(item -> LineItem.builder()
                        .description(item.description())
                        .amount(item.amount())
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

        return mapper.toResponse(repository.save(transaction));
    }
}
