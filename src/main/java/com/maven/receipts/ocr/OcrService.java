package com.maven.receipts.ocr;

import com.maven.receipts.entity.Receipt;

public interface OcrService {
    String extractText(Receipt receipt);
}
