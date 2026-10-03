package com.maven.receipts.processing;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ParsedReceipt(
        String merchant,
        LocalDate date,
        String currency,
        BigDecimal grandTotal,
        List<ParsedTax> taxes,
        List<ParsedLineItem> lineItems) {

    public record ParsedTax(
            String name,
            BigDecimal rate,
            BigDecimal amount) {
    }

    public record ParsedLineItem(
            String description,
            BigDecimal amount) {
    }
}
