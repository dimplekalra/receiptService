package com.maven.receipts.integrationTests;

import com.maven.receipts.dto.TransactionResponse;
import com.maven.receipts.enums.ItemizeStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TaxOnlyReceiptIntegrationTest extends IntegrationTestBase {
        @Test
        void taxOnlyReceiptShouldNeedReview() throws Exception {

                Long receiptId = api.uploadReceipt("receipt-tax-only.txt");

                TransactionResponse transaction = api.processReceipt(receiptId);

                assertEquals(
                                ItemizeStatus.NEEDS_REVIEW,
                                transaction.itemizeStatus());

                assertTrue(
                                transaction.lineItems().isEmpty());

                assertEquals(
                                1,
                                transaction.taxes().size());
        }
}
