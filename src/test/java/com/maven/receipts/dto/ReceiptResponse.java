package com.maven.receipts.dto;

public record ReceiptResponse(

        Long id,

        String originalFilename,

        boolean processed,

        Long transactionId) {
}
