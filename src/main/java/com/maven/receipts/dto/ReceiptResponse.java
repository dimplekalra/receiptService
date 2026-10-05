package com.maven.receipts.dto;

import java.time.Instant;

public record ReceiptResponse(
                Long id,
                String originalFilename,
                Instant uploadedAt,
                boolean processed,
                Long transactionId) {
}
