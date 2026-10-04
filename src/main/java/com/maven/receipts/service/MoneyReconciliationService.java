package com.maven.receipts.service;

import java.math.BigDecimal;
import java.util.List;

import com.maven.receipts.entity.ExpenseTransaction;
import com.maven.receipts.entity.LineItem;
import com.maven.receipts.entity.Tax;

public interface MoneyReconciliationService {
        BigDecimal calculateItemTotal(List<LineItem> items);

        BigDecimal calculateTaxTotal(List<Tax> taxes);

        ReconciliationResult reconcile(
                        ExpenseTransaction transaction,
                        List<LineItem> candidateItems);

        ReconciliationResult reconcile(
                        ExpenseTransaction transaction);

        record ReconciliationResult(
                        boolean matches,
                        BigDecimal expected,
                        BigDecimal actual,
                        BigDecimal difference) {
        }
}
