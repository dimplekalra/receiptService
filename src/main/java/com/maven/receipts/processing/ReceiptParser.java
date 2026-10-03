package com.maven.receipts.processing;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ReceiptParser {

    private static final Pattern MERCHANT = Pattern.compile("^MERCHANT:\\s*(.+)$", Pattern.MULTILINE);

    private static final Pattern DATE = Pattern.compile("^DATE:\\s*(\\d{4}-\\d{2}-\\d{2})$", Pattern.MULTILINE);

    private static final Pattern CURRENCY = Pattern.compile("^CURRENCY:\\s*([A-Z]{3})$", Pattern.MULTILINE);

    private static final Pattern TOTAL = Pattern.compile("^TOTAL\\s+([0-9]+(?:\\.[0-9]+)?)$", Pattern.MULTILINE);

    private static final Pattern TAX = Pattern.compile(
            "^(?:VAT|incl\\.\\s*VAT)\\s+(\\d+(?:\\.\\d+)?)%\\s+([0-9]+(?:\\.[0-9]+)?)$",
            Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);

    /*
     * Item lines look like:
     *
     * Espresso 3.50
     * Sandwich 8.90
     *
     * We deliberately stop parsing before Subtotal/VAT/TOTAL.
     */
    private static final Pattern ITEM = Pattern.compile(
            "^(.+?)\\s{2,}([0-9]+(?:\\.[0-9]+)?)$",
            Pattern.MULTILINE);

    public ParsedReceipt parse(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("OCR text is empty");
        }

        String merchant = required(
                find(MERCHANT, text),
                "merchant").trim();

        String dateText = required(
                find(DATE, text),
                "date");

        LocalDate date;
        try {
            date = LocalDate.parse(dateText);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid receipt date: " + dateText);
        }

        String currency = required(
                find(CURRENCY, text),
                "currency").trim();

        BigDecimal grandTotal = decimal(
                required(find(TOTAL, text), "grand total"),
                "grand total");

        List<ParsedReceipt.ParsedTax> taxes = parseTaxes(text);
        List<ParsedReceipt.ParsedLineItem> items = parseItems(text);

        return new ParsedReceipt(
                merchant,
                date,
                currency,
                grandTotal,
                taxes,
                items);
    }

    private List<ParsedReceipt.ParsedTax> parseTaxes(String text) {
        List<ParsedReceipt.ParsedTax> taxes = new ArrayList<>();

        Matcher matcher = TAX.matcher(text);

        while (matcher.find()) {
            BigDecimal rate = new BigDecimal(matcher.group(1));
            BigDecimal amount = new BigDecimal(matcher.group(2));

            taxes.add(
                    new ParsedReceipt.ParsedTax(
                            "VAT",
                            rate,
                            amount));
        }

        return taxes;
    }

    private List<ParsedReceipt.ParsedLineItem> parseItems(String text) {
        List<ParsedReceipt.ParsedLineItem> items = new ArrayList<>();

        String[] lines = text.split("\\R");

        for (String line : lines) {
            String trimmed = line.trim();

            if (trimmed.isEmpty()) {
                continue;
            }

            /*
             * Ignore receipt metadata and summary lines.
             */
            if (trimmed.startsWith("MERCHANT:")
                    || trimmed.startsWith("DATE:")
                    || trimmed.startsWith("CURRENCY:")
                    || trimmed.startsWith("Subtotal")
                    || trimmed.startsWith("VAT")
                    || trimmed.startsWith("TOTAL")
                    || trimmed.startsWith("incl. VAT")
                    || trimmed.startsWith("(")) {
                continue;
            }

            Matcher matcher = ITEM.matcher(line);

            if (!matcher.matches()) {
                continue;
            }

            String description = matcher.group(1).trim();
            BigDecimal amount = new BigDecimal(matcher.group(2));

            /*
             * "Trip fare" in the tax-only fixture has no amount,
             * so it naturally does not become an item.
             */
            items.add(
                    new ParsedReceipt.ParsedLineItem(
                            description,
                            amount));
        }

        return items;
    }

    private String find(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);

        if (!matcher.find()) {
            return null;
        }

        return matcher.group(1);
    }

    private String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Unable to extract required field: " + field);
        }

        return value;
    }

    private BigDecimal decimal(String value, String field) {
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Invalid " + field + ": " + value);
        }
    }
}
