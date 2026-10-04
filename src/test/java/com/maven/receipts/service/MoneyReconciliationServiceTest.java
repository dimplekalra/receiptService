package com.maven.receipts.service;

import com.maven.receipts.entity.ExpenseTransaction;
import com.maven.receipts.entity.LineItem;
import com.maven.receipts.entity.Tax;
import com.maven.receipts.service.impl.MoneyReconciliationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MoneyReconciliationServiceTest {
    private MoneyReconciliationService service;

    @BeforeEach
    void setup() {
        service = new MoneyReconciliationServiceImpl();
    }

    @Test
    void shouldCalculateItemTotal() {

        List<LineItem> items = List.of(
                LineItem.builder().amount(new BigDecimal("10.00")).build(),
                LineItem.builder().amount(new BigDecimal("5.50")).build(),
                LineItem.builder().amount(new BigDecimal("2.25")).build());

        assertEquals(
                new BigDecimal("17.75"),
                service.calculateItemTotal(items));
    }

    @Test
    void shouldCalculateTaxTotal() {

        List<Tax> taxes = List.of(
                Tax.builder().amount(new BigDecimal("1.50")).build(),
                Tax.builder().amount(new BigDecimal("0.75")).build());

        assertEquals(
                new BigDecimal("2.25"),
                service.calculateTaxTotal(taxes));
    }

    @Test
    void shouldReconcileSuccessfully() {

        ExpenseTransaction transaction = ExpenseTransaction.builder()
                .grandTotal(new BigDecimal("12.00"))
                .build();

        transaction.addTax(
                Tax.builder()
                        .amount(new BigDecimal("2.00"))
                        .build());

        List<LineItem> items = List.of(
                LineItem.builder()
                        .amount(new BigDecimal("10.00"))
                        .build());

        MoneyReconciliationService.ReconciliationResult result = service.reconcile(transaction, items);

        assertTrue(result.matches());
    }

    @Test
    void shouldDetectMismatch() {

        ExpenseTransaction transaction = ExpenseTransaction.builder()
                .grandTotal(new BigDecimal("20.00"))
                .build();

        transaction.addTax(
                Tax.builder()
                        .amount(new BigDecimal("2.00"))
                        .build());

        List<LineItem> items = List.of(
                LineItem.builder()
                        .amount(new BigDecimal("10.00"))
                        .build());

        MoneyReconciliationService.ReconciliationResult result = service.reconcile(transaction, items);

        assertFalse(result.matches());

        assertEquals(
                new BigDecimal("20.00"),
                result.expected());

        assertEquals(
                new BigDecimal("12.00"),
                result.actual());

        assertEquals(
                new BigDecimal("8.00"),
                result.difference());
    }
}
