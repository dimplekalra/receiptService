package com.maven.receipts.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    String store(MultipartFile file);

    Resource load(String storedFilename);

    void delete(String storedFilename);
}
