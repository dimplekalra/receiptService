package com.maven.receipts.service.impl;

import com.maven.receipts.dto.TransactionResponse;
import com.maven.receipts.entity.ExpenseTransaction;
import com.maven.receipts.enums.ItemizeStatus;
import com.maven.receipts.exception.MoneyMismatchException;
import com.maven.receipts.exception.TransactionNotFoundException;
import com.maven.receipts.mapper.TransactionMapper;
import com.maven.receipts.repository.ExpenseTransactionRepository;
import com.maven.receipts.service.MoneyReconciliationService;
import com.maven.receipts.service.TransactionCompleteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransactionCompleteServiceImpl implements TransactionCompleteService {
    private final ExpenseTransactionRepository repository;
    private final MoneyReconciliationService reconciliationService;
    private final TransactionMapper mapper;

    @Override
    @Transactional
    public TransactionResponse complete(Long transactionId) {

        ExpenseTransaction transaction = repository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException(transactionId));

        MoneyReconciliationService.ReconciliationResult result = reconciliationService.reconcile(transaction);

        if (!result.matches()) {

            throw new MoneyMismatchException(
                    result.expected(),
                    result.actual(),
                    result.difference());
        }

        transaction.setItemizeStatus(ItemizeStatus.COMPLETE);

        ExpenseTransaction saved = repository.save(transaction);

        return mapper.toResponse(saved);
    }
}
