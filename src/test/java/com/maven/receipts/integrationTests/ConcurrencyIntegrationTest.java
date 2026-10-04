package com.maven.receipts.integrationTests;

import com.maven.receipts.dto.TransactionResponse;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConcurrencyIntegrationTest extends IntegrationTestBase {
    @Test
    void concurrentProcessingCreatesOnlyOneTransaction()
            throws Exception {

        Long receiptId = api.uploadReceipt("receipt-clean.txt");

        ExecutorService executor = Executors.newFixedThreadPool(2);

        Callable<TransactionResponse> task = () -> api.processReceipt(receiptId);

        Future<TransactionResponse> first = executor.submit(task);

        Future<TransactionResponse> second = executor.submit(task);

        TransactionResponse tx1 = first.get();
        TransactionResponse tx2 = second.get();

        executor.shutdown();

        assertEquals(
                tx1.id(),
                tx2.id());

        assertEquals(
                tx1.lineItems().size(),
                tx2.lineItems().size());

        assertEquals(
                tx1.taxes().size(),
                tx2.taxes().size());
    }
}
