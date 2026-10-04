package com.maven.receipts.service.impl;

import com.maven.receipts.entity.ExpenseTransaction;
import com.maven.receipts.entity.LineItem;
import com.maven.receipts.entity.Tax;
import com.maven.receipts.service.MoneyReconciliationService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class MoneyReconciliationServiceImpl implements MoneyReconciliationService {
        @Override
        public BigDecimal calculateItemTotal(List<LineItem> items) {

                return items.stream()
                                .map(LineItem::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        @Override
        public BigDecimal calculateTaxTotal(List<Tax> taxes) {

                return taxes.stream()
                                .map(Tax::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        @Override
        public ReconciliationResult reconcile(
                        ExpenseTransaction transaction,
                        List<LineItem> candidateItems) {

                BigDecimal itemTotal = calculateItemTotal(candidateItems);

                BigDecimal taxTotal = calculateTaxTotal(transaction.getTaxes());

                BigDecimal expected = transaction.getGrandTotal();

                BigDecimal actual = itemTotal.add(taxTotal);

                BigDecimal difference = expected.subtract(actual);

                return new ReconciliationResult(
                                difference.compareTo(BigDecimal.ZERO) == 0,
                                expected,
                                actual,
                                difference);
        }

        @Override
        public ReconciliationResult reconcile(
                        ExpenseTransaction transaction) {

                return reconcile(
                                transaction,
                                transaction.getLineItems());
        }
}
