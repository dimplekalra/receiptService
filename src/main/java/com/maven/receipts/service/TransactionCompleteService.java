package com.maven.receipts.service;

import com.maven.receipts.dto.TransactionResponse;

public interface TransactionCompleteService {
    TransactionResponse complete(Long transactionId);
}
