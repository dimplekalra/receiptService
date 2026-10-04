package com.maven.receipts.integrationTests;

import com.maven.receipts.dto.TransactionResponse;
import com.maven.receipts.enums.ItemizeStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class ReceiptIntegrationTest extends IntegrationTestBase {
        @Test
        void cleanReceiptShouldProcessSuccessfully() throws Exception {

                Long receiptId = api.uploadReceipt("receipt-clean.txt");

                TransactionResponse transaction = api.processReceipt(receiptId);

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
                                transaction.grandTotal()
                                                .compareTo(new BigDecimal("17.85")));

                assertEquals(
                                1,
                                transaction.taxes().size());

                assertEquals(
                                3,
                                transaction.lineItems().size());
        }
}
