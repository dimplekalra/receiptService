package com.maven.receipts.exception;

import java.math.BigDecimal;

public class MoneyMismatchException extends RuntimeException {
    private final BigDecimal expected;
    private final BigDecimal actual;
    private final BigDecimal difference;

    public MoneyMismatchException(
            BigDecimal expected,
            BigDecimal actual,
            BigDecimal difference) {
        super("Line items do not reconcile with transaction total.");

        this.expected = expected;
        this.actual = actual;
        this.difference = difference;
    }

    public BigDecimal getExpected() {
        return expected;
    }

    public BigDecimal getActual() {
        return actual;
    }

    public BigDecimal getDifference() {
        return difference;
    }
}
