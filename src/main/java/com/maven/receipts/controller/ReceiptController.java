package com.maven.receipts.controller;

import com.maven.receipts.dto.OcrResponse;
import com.maven.receipts.dto.ReceiptResponse;
import com.maven.receipts.dto.ReceiptUploadResponse;
import com.maven.receipts.dto.TransactionResponse;
import com.maven.receipts.service.ReceiptDeleteService;
import com.maven.receipts.service.ReceiptProcessingService;
import com.maven.receipts.service.ReceiptService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/receipts")
@RequiredArgsConstructor
public class ReceiptController {
    private final ReceiptService receiptService;
    private final ReceiptProcessingService receiptProcessingService;
    private final ReceiptDeleteService receiptDeleteService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReceiptUploadResponse> uploadReceipt(
            @RequestParam("file") MultipartFile file) {

        ReceiptUploadResponse response = receiptService.upload(file);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/{receiptId}/process")
    public ResponseEntity<TransactionResponse> processReceipt(
            @PathVariable Long receiptId) {
        return ResponseEntity.ok(
                receiptProcessingService.process(receiptId));
    }

    @GetMapping("/{receiptId}")
    public ReceiptResponse getReceipt(
            @PathVariable Long receiptId) {
        return receiptService.getReceipt(receiptId);
    }

    @GetMapping("/{receiptId}/ocr")
    public OcrResponse getOcr(
            @PathVariable Long receiptId) {
        return receiptService.getOcr(receiptId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReceipt(
            @PathVariable Long id) {

        receiptDeleteService.delete(id);

    }
}
