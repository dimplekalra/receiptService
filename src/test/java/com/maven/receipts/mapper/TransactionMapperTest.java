package com.maven.receipts.mapper;

import com.maven.receipts.dto.TransactionResponse;
import com.maven.receipts.entity.*;
import com.maven.receipts.enums.ItemizeStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class TransactionMapperTest {
    private final TransactionMapper mapper = new TransactionMapper();

    @Test
    void shouldMapTransaction() {

        Receipt receipt = Receipt.builder()
                .id(1L)
                .build();

        ExpenseTransaction transaction = ExpenseTransaction.builder()
                .id(10L)
                .receipt(receipt)
                .merchant("Cafe")
                .currency("EUR")
                .transactionDate(LocalDate.now())
                .grandTotal(new BigDecimal("15.00"))
                .itemizeStatus(ItemizeStatus.COMPLETE)
                .build();

        transaction.addTax(
                Tax.builder()
                        .id(1L)
                        .name("VAT")
                        .rate(new BigDecimal("0.19"))
                        .amount(new BigDecimal("2.40"))
                        .build());

        transaction.addLineItem(
                LineItem.builder()
                        .id(1L)
                        .description("Coffee")
                        .amount(new BigDecimal("12.60"))
                        .build());

        TransactionResponse response = mapper.toResponse(transaction);

        assertEquals(10L, response.id());
        assertEquals(1L, response.receiptId());
        assertEquals("Cafe", response.merchant());
        assertEquals("EUR", response.currency());

        assertEquals(1, response.taxes().size());
        assertEquals(1, response.lineItems().size());
    }
}
