package com.maven.receipts.controller;

import com.maven.receipts.dto.TransactionResponse;
import com.maven.receipts.dto.UpdateItemsRequest;
import com.maven.receipts.service.TransactionCompleteService;
import com.maven.receipts.service.TransactionItemizeService;
import com.maven.receipts.service.TransactionQueryService;
import com.maven.receipts.service.TransactionUpdateService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {
        private final TransactionQueryService service;
        private final TransactionUpdateService updateService;
        private final TransactionItemizeService transactionItemizeService;
        private final TransactionCompleteService transactionCompleteService;

        @GetMapping("/{transactionId}")
        public TransactionResponse getTransaction(

                        @PathVariable Long transactionId

        ) {

                return service.getTransaction(
                                transactionId);

        }

        @GetMapping
        public TransactionResponse getByReceipt(
                        @RequestParam("receipt_id") Long receiptId) {

                return service.getByReceiptId(
                                receiptId);

        }

        @PatchMapping("/{transactionId}/items")
        public TransactionResponse updateItems(

                        @PathVariable Long transactionId,

                        @Valid @RequestBody UpdateItemsRequest request

        ) {

                return updateService.updateItems(
                                transactionId,
                                request);

        }

        @PostMapping("/{id}/itemize")
        public TransactionResponse reItemize(
                        @PathVariable Long id) {

                return transactionItemizeService.reItemize(id);
        }

        @PostMapping("/{id}/complete")
        public TransactionResponse complete(
                        @PathVariable Long id) {

                return transactionCompleteService.complete(id);
        }
}
