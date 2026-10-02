package com.maven.receipts.service;

import com.maven.receipts.dto.ReceiptUploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ReceiptService {
    ReceiptUploadResponse upload(MultipartFile file);
}
