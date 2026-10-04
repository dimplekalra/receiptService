package com.maven.receipts.exception;

public class TransactionNotFoundException extends RuntimeException {
    public TransactionNotFoundException(Long id) {
        super("Transaction " + id + " not found");
    }
}
