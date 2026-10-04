package com.maven.receipts.exception;

public class ProcessedReceiptDeletionException extends RuntimeException {
    public ProcessedReceiptDeletionException(Long receiptId) {

        super("Receipt " + receiptId +
                " has already been processed.");
    }
}
