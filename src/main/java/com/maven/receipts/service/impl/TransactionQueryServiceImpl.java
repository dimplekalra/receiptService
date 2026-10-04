package com.maven.receipts.service.impl;

import com.maven.receipts.dto.TransactionResponse;
import com.maven.receipts.entity.ExpenseTransaction;
import com.maven.receipts.exception.TransactionNotFoundException;
import com.maven.receipts.mapper.TransactionMapper;
import com.maven.receipts.repository.ExpenseTransactionRepository;
import com.maven.receipts.service.TransactionQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransactionQueryServiceImpl implements TransactionQueryService {
    private final ExpenseTransactionRepository repository;

    private final TransactionMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransaction(
            Long transactionId) {

        ExpenseTransaction transaction = repository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException(
                        transactionId));

        return mapper.toResponse(transaction);

    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getByReceiptId(
            Long receiptId) {

        ExpenseTransaction transaction = repository.findByReceiptId(receiptId)
                .orElseThrow(() -> new TransactionNotFoundException(
                        receiptId));

        return mapper.toResponse(transaction);

    }
}
