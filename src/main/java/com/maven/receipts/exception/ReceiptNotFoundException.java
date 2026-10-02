package com.maven.receipts.exception;

public class ReceiptNotFoundException extends RuntimeException {
    public ReceiptNotFoundException(Long receiptId) {
        super("Receipt with id " + receiptId + " was not found");
    }
}
