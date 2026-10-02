package com.maven.receipts.controller;

import com.maven.receipts.dto.ReceiptUploadResponse;
import com.maven.receipts.service.ReceiptService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/receipts")
public class ReceiptController {
    private final ReceiptService receiptService;

    public ReceiptController(ReceiptService receiptService) {
        this.receiptService = receiptService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReceiptUploadResponse> uploadReceipt(
            @RequestParam("file") MultipartFile file) {

        ReceiptUploadResponse response = receiptService.upload(file);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
