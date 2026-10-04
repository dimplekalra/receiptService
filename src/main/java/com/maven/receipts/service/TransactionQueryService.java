package com.maven.receipts.service;

import com.maven.receipts.dto.TransactionResponse;

public interface TransactionQueryService {
    TransactionResponse getTransaction(
            Long transactionId);

    TransactionResponse getByReceiptId(
            Long receiptId);
}
