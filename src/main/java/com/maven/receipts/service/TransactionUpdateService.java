package com.maven.receipts.service;

import com.maven.receipts.dto.TransactionResponse;
import com.maven.receipts.dto.UpdateItemsRequest;

public interface TransactionUpdateService {
    TransactionResponse updateItems(
            Long transactionId,
            UpdateItemsRequest request);

}
