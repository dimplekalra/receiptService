package com.maven.receipts.dto;

import java.time.Instant;

public record ReceiptUploadResponse(
        Long receiptId,
        boolean processed,
        Instant uploadedAt) {
}
