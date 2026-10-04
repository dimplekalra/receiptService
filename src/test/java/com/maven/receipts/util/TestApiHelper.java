package com.maven.receipts.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maven.receipts.dto.ReceiptResponse;
import com.maven.receipts.dto.TransactionResponse;
import com.maven.receipts.dto.UploadResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.InputStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Component
public class TestApiHelper {
        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        /*
         * Uploads a receipt fixture.
         *
         * Returns:
         * receiptId
         */
        public Long uploadReceipt(String fixtureName) throws Exception {

                ClassPathResource resource = new ClassPathResource("fixtures/task-a/" + fixtureName);

                InputStream inputStream = resource.getInputStream();

                MockMultipartFile file = new MockMultipartFile(
                                "file",
                                fixtureName,
                                MediaType.TEXT_PLAIN_VALUE,
                                inputStream);

                MvcResult result = mockMvc.perform(
                                multipart("/receipts")
                                                .file(file))
                                .andExpect(status().isCreated())
                                .andReturn();

                UploadResponse response = objectMapper.readValue(
                                result.getResponse().getContentAsString(),
                                UploadResponse.class);

                return response.receiptId();
        }

        /*
         * POST /receipts/{id}/process
         */
        public TransactionResponse processReceipt(Long receiptId)
                        throws Exception {

                MvcResult result = mockMvc.perform(
                                post("/receipts/{id}/process",
                                                receiptId))
                                .andExpect(status().isOk())
                                .andReturn();

                return objectMapper.readValue(
                                result.getResponse().getContentAsString(),
                                TransactionResponse.class);
        }

        /*
         * GET /transactions/{id}
         */
        public TransactionResponse getTransaction(Long transactionId)
                        throws Exception {

                MvcResult result = mockMvc.perform(
                                get("/transactions/{id}",
                                                transactionId))
                                .andExpect(status().isOk())
                                .andReturn();

                return objectMapper.readValue(
                                result.getResponse().getContentAsString(),
                                TransactionResponse.class);
        }

        /*
         * GET /transactions?receipt_id=
         */
        public TransactionResponse getTransactionByReceipt(
                        Long receiptId)
                        throws Exception {

                MvcResult result = mockMvc.perform(
                                get("/transactions")
                                                .param(
                                                                "receipt_id",
                                                                receiptId.toString()))
                                .andExpect(status().isOk())
                                .andReturn();

                return objectMapper.readValue(
                                result.getResponse().getContentAsString(),
                                TransactionResponse.class);
        }

        /*
         * GET /receipts/{id}
         */
        public ReceiptResponse getReceipt(Long receiptId)
                        throws Exception {

                MvcResult result = mockMvc.perform(
                                get("/receipts/{id}",
                                                receiptId))
                                .andExpect(status().isOk())
                                .andReturn();

                return objectMapper.readValue(
                                result.getResponse().getContentAsString(),
                                ReceiptResponse.class);
        }

        /*
         * DELETE /receipts/{id}
         */
        public void deleteReceipt(Long receiptId)
                        throws Exception {

                mockMvc.perform(
                                delete("/receipts/{id}",
                                                receiptId))
                                .andExpect(status().isNoContent());
        }
}
