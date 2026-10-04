package com.maven.receipts.service;

import com.maven.receipts.dto.TransactionResponse;

public interface TransactionItemizeService {

    TransactionResponse reItemize(Long transactionId);

}
