package com.maven.receipts.dto;

public record OcrResponse(
        Long receiptId,
        String ocrText) {
}
