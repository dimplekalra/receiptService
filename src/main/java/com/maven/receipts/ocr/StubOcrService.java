package com.maven.receipts.ocr;

import com.maven.receipts.config.StorageProperties;
import com.maven.receipts.entity.Receipt;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class StubOcrService implements OcrService {

    private final StorageProperties storageProperties;

    public StubOcrService(StorageProperties storageProperties) {
        this.storageProperties = storageProperties;
    }

    @Override
    public String extractText(Receipt receipt) {
        try {
            Path path = Path.of(
                    storageProperties.getUploadDirectory(),
                    receipt.getStoredFilename());

            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Unable to read stored receipt for OCR: " + receipt.getId(),
                    e);
        }
    }
}
