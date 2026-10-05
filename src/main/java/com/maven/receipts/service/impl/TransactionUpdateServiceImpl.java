package com.maven.receipts.service.impl;

import com.maven.receipts.dto.TransactionResponse;
import com.maven.receipts.dto.UpdateItemsRequest;
import com.maven.receipts.entity.ExpenseTransaction;
import com.maven.receipts.entity.LineItem;
import com.maven.receipts.exception.MoneyMismatchException;
import com.maven.receipts.exception.TransactionNotFoundException;
import com.maven.receipts.mapper.LineItemMapper;
import com.maven.receipts.mapper.TransactionMapper;
import com.maven.receipts.repository.ExpenseTransactionRepository;
import com.maven.receipts.service.MoneyReconciliationService;
import com.maven.receipts.service.TransactionUpdateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionUpdateServiceImpl implements TransactionUpdateService {
        private final ExpenseTransactionRepository repository;

        private final LineItemMapper lineItemMapper;

        private final TransactionMapper transactionMapper;

        private final MoneyReconciliationService reconciliationService;

        @Override
        @Transactional
        public TransactionResponse updateItems(
                        Long transactionId,
                        UpdateItemsRequest request) {

                ExpenseTransaction transaction = repository.findById(transactionId)
                                .orElseThrow(() -> new TransactionNotFoundException(transactionId));

                /*
                 * Build candidate items.
                 * DO NOT modify the database yet.
                 */
                List<LineItem> candidateItems = request.items()
                                .stream()
                                .map(lineItemMapper::toEntity)
                                .toList();

                MoneyReconciliationService.ReconciliationResult result = reconciliationService.reconcile(
                                transaction,
                                candidateItems);

                if (!result.matches()) {

                        throw new MoneyMismatchException(
                                        result.expected(),
                                        result.actual(),
                                        result.difference());

                }

                /*
                 * Validation succeeded.
                 * Now replace line items.
                 */

                transaction.getLineItems().clear();

                for (LineItem item : candidateItems) {

                        transaction.addLineItem(item);

                }

                ExpenseTransaction saved = repository.save(transaction);

                return transactionMapper.toResponse(saved);

        }
}
