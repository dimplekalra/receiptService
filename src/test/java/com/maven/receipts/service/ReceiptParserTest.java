package com.maven.receipts.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.maven.receipts.processing.ParsedReceipt;
import com.maven.receipts.processing.ReceiptParser;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class ReceiptParserTest {
    private ReceiptParser parser;

    @BeforeEach
    void setup() {
        parser = new ReceiptParser();
    }

    @Test
    void shouldParseCompleteReceipt() {

        String receipt = """
                MERCHANT: Cafe Mitte
                DATE: 2026-03-12
                CURRENCY: EUR

                Espresso  3.50
                Sandwich  8.90
                Mineral water  2.60

                VAT 19% 2.85
                TOTAL 17.85
                """;

        ParsedReceipt parsed = parser.parse(receipt);

        assertEquals("Cafe Mitte", parsed.merchant());
        assertEquals("EUR", parsed.currency());
        assertEquals(new BigDecimal("17.85"), parsed.grandTotal());

        assertEquals(3, parsed.lineItems().size());

        assertEquals("Espresso",
                parsed.lineItems().get(0).description());

        assertEquals(
                new BigDecimal("3.50"),
                parsed.lineItems().get(0).amount());

        assertEquals(1, parsed.taxes().size());

        assertEquals(
                new BigDecimal("2.85"),
                parsed.taxes().get(0).amount());
    }

    @Test
    void shouldThrowWhenMerchantMissing() {

        String receipt = """
                DATE: 2026-03-12
                CURRENCY: EUR
                TOTAL 10.00
                """;

        assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse(receipt));
    }

    @Test
    void shouldThrowForBlankText() {

        assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse(""));
    }

    @Test
    void shouldThrowForInvalidDate() {

        String receipt = """
                MERCHANT: Cafe
                DATE: invalid-date
                CURRENCY: EUR
                TOTAL 10.00
                """;

        assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse(receipt));
    }
}
