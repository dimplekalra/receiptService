package com.maven.receipts.integrationTests;

import com.maven.receipts.dto.TransactionResponse;
import com.maven.receipts.enums.ItemizeStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class ReceiptMismatchIntegrationTest extends IntegrationTestBase {
        @Test
        void mismatchReceiptShouldNeedReview() throws Exception {

                Long receiptId = api.uploadReceipt("receipt-mismatch.txt");

                TransactionResponse transaction = api.processReceipt(receiptId);

                assertEquals(
                                ItemizeStatus.NEEDS_REVIEW,
                                transaction.itemizeStatus());

                assertEquals(
                                2,
                                transaction.lineItems().size());

                assertEquals(
                                0,
                                transaction.grandTotal()
                                                .compareTo(new BigDecimal("18.50")));
        }
}
