package com.maven.receipts.dto;

public record UploadResponse(
        Long receiptId,
        boolean processed,
        String uploadedAt) {
}
