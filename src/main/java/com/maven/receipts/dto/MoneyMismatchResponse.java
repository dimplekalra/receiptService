package com.maven.receipts.dto;

import java.math.BigDecimal;

public record MoneyMismatchResponse(
        String error,
        BigDecimal expected,
        BigDecimal actual,
        BigDecimal difference) {

}
