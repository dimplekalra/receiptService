package com.maven.receipts.service;

import com.maven.receipts.dto.TransactionResponse;

public interface ReceiptProcessingService {

    TransactionResponse process(Long receiptId);
}
