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
        void shouldMapTransactionToResponse() {

                Receipt receipt = Receipt.builder()
                                .id(10L)
                                .build();

                ExpenseTransaction tx = ExpenseTransaction.builder()
                                .id(1L)
                                .receipt(receipt)
                                .merchant("Cafe")
                                .transactionDate(LocalDate.now())
                                .currency("EUR")
                                .grandTotal(new BigDecimal("15.50"))
                                .itemizeStatus(ItemizeStatus.COMPLETE)
                                .build();

                tx.addTax(
                                Tax.builder()
                                                .name("VAT")
                                                .rate(new BigDecimal("19"))
                                                .amount(new BigDecimal("2.50"))
                                                .build());

                tx.addLineItem(
                                LineItem.builder()
                                                .description("Coffee")
                                                .amount(new BigDecimal("13"))
                                                .build());

                TransactionResponse response = mapper.toResponse(tx);

                assertEquals(1L, response.id());

                assertEquals(10L, response.receiptId());

                assertEquals("Cafe", response.merchant());

                assertEquals(1, response.taxes().size());

                assertEquals(1, response.lineItems().size());
        }
}
