package com.maven.receipts.integrationTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maven.receipts.dto.TransactionResponse;
import com.maven.receipts.dto.UpdateItemsRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TransactionIntegrationTest extends IntegrationTestBase {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void processingSameReceiptTwiceShouldCreateOnlyOneTransaction()
            throws Exception {

        Long receiptId = api.uploadReceipt("receipt-clean.txt");

        TransactionResponse first = api.processReceipt(receiptId);

        TransactionResponse second = api.processReceipt(receiptId);

        assertEquals(first.id(), second.id());

        assertEquals(
                first.lineItems().size(),
                second.lineItems().size());

        assertEquals(
                first.taxes().size(),
                second.taxes().size());
    }

    @Test
    void getTransactionByReceiptShouldReturnSameTransaction()
            throws Exception {

        Long receiptId = api.uploadReceipt("receipt-clean.txt");

        TransactionResponse processed = api.processReceipt(receiptId);

        TransactionResponse lookup = api.getTransactionByReceipt(receiptId);

        assertEquals(
                processed.id(),
                lookup.id());
    }

    @Test
    void deleteProcessedReceiptShouldReturn409()
            throws Exception {

        Long receiptId = api.uploadReceipt("receipt-clean.txt");

        api.processReceipt(receiptId);

        mockMvc.perform(
                delete("/receipts/{id}", receiptId))
                .andExpect(status().isConflict());
    }

    @Test
    void completeShouldReturn409ForMismatchReceipt()
            throws Exception {

        Long receiptId = api.uploadReceipt("receipt-mismatch.txt");

        TransactionResponse tx = api.processReceipt(receiptId);

        mockMvc.perform(
                post("/transactions/{id}/complete",
                        tx.id()))
                .andExpect(status().isConflict());
    }

    @Test
    void patchShouldReturn409WhenMoneyDoesNotReconcile()
            throws Exception {

        Long receiptId = api.uploadReceipt("receipt-clean.txt");

        TransactionResponse tx = api.processReceipt(receiptId);

        UpdateItemsRequest request = new UpdateItemsRequest(

                List.of(

                        new UpdateItemsRequest.ItemRequest(

                                "Fake",

                                new BigDecimal("100.00"),

                                null,

                                null)));

        mockMvc.perform(

                patch("/transactions/{id}/items",
                        tx.id())

                        .contentType(
                                MediaType.APPLICATION_JSON)

                        .content(
                                objectMapper.writeValueAsString(
                                        request)))

                .andExpect(status().isConflict());
    }
}
