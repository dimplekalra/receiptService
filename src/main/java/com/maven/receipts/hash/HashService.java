package com.maven.receipts.hash;

import org.springframework.web.multipart.MultipartFile;

public interface HashService {
    String sha256(MultipartFile file);
}
