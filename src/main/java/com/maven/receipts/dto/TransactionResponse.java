package com.maven.receipts.dto;

import com.maven.receipts.enums.ItemizeStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record TransactionResponse(
        Long id,
        Long receiptId,
        String merchant,
        LocalDate date,
        String currency,
        BigDecimal grandTotal,
        ItemizeStatus itemizeStatus,
        List<TaxResponse> taxes,
        List<LineItemResponse> lineItems) {

    public record TaxResponse(
            Long id,
            String name,
            BigDecimal rate,
            BigDecimal amount) {
    }

    public record LineItemResponse(
            Long id,
            String description,
            BigDecimal amount,
            BigDecimal quantity,
            BigDecimal taxAmount) {
    }
}
