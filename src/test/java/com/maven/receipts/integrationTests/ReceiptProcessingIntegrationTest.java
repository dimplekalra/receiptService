package com.maven.receipts.integrationTests;

import com.maven.receipts.dto.TransactionResponse;
import com.maven.receipts.enums.ItemizeStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class ReceiptProcessingIntegrationTest extends IntegrationTestBase {
    @Test
    void shouldProcessCleanReceiptSuccessfully() throws Exception {

        /*
         * Arrange
         */
        Long receiptId = api.uploadReceipt("receipt-clean.txt");

        /*
         * Act
         */
        TransactionResponse transaction = api.processReceipt(receiptId);

        /*
         * Assert Header
         */
        assertEquals(
                "Cafe Mitte",
                transaction.merchant());

        assertEquals(
                "EUR",
                transaction.currency());

        assertEquals(
                ItemizeStatus.COMPLETE,
                transaction.itemizeStatus());

        assertEquals(
                0,
                transaction.grandTotal().compareTo(
                        new BigDecimal("17.85")));

        /*
         * Taxes
         */
        assertEquals(
                1,
                transaction.taxes().size());

        assertEquals(
                "VAT",
                transaction.taxes().get(0).name());

        assertEquals(
                0,
                transaction.taxes().get(0)
                        .amount()
                        .compareTo(new BigDecimal("2.85")));

        /*
         * Line Items
         */
        assertEquals(
                3,
                transaction.lineItems().size());

        assertEquals(
                "Espresso",
                transaction.lineItems().get(0).description());

        assertEquals(
                "Sandwich",
                transaction.lineItems().get(1).description());

        assertEquals(
                "Mineral water",
                transaction.lineItems().get(2).description());
    }
}
