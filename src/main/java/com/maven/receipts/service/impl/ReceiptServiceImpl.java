package com.maven.receipts.service.impl;

import com.maven.receipts.dto.ReceiptUploadResponse;
import com.maven.receipts.entity.Receipt;
import com.maven.receipts.exception.InvalidReceiptFileException;
import com.maven.receipts.hash.HashService;
import com.maven.receipts.repository.ReceiptRepository;
import com.maven.receipts.service.ReceiptService;
import com.maven.receipts.storage.StorageService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;

@Service
public class ReceiptServiceImpl implements ReceiptService {
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    private static final String[] ALLOWED_EXTENSIONS = {
            "pdf",
            "png",
            "jpg",
            "jpeg",
            "txt"
    };

    private final ReceiptRepository receiptRepository;
    private final HashService hashService;
    private final StorageService storageService;

    public ReceiptServiceImpl(
            ReceiptRepository receiptRepository,
            HashService hashService,
            StorageService storageService) {
        this.receiptRepository = receiptRepository;
        this.hashService = hashService;
        this.storageService = storageService;
    }

    @Override
    @Transactional
    public ReceiptUploadResponse upload(MultipartFile file) {

        validateFile(file);

        String fileHash = hashService.sha256(file);

        /*
         * Fast path for the normal duplicate-upload case.
         *
         * Example:
         * Client uploads receipt.pdf
         * Network fails
         * Client retries exactly the same bytes
         *
         * We return the original receipt instead of creating another one.
         */
        return receiptRepository.findByFileHash(fileHash)
                .map(this::toResponse)
                .orElseGet(() -> createReceipt(file, fileHash));
    }

    private ReceiptUploadResponse createReceipt(
            MultipartFile file,
            String fileHash) {
        String storedFilename = storageService.store(file);

        try {
            Receipt receipt = Receipt.builder()
                    .fileHash(fileHash)
                    .originalFilename(safeOriginalFilename(file))
                    .storedFilename(storedFilename)
                    .storagePath(storedFilename)
                    .processed(false)
                    .build();

            Receipt savedReceipt = receiptRepository.saveAndFlush(receipt);

            return toResponse(savedReceipt);

        } catch (DataIntegrityViolationException ex) {

            /*
             * This handles the important race:
             *
             * Request A ── check ── none ── insert
             * Request B ── check ── none ── insert
             *
             * The database UNIQUE(file_hash) constraint decides
             * the winner. The losing request retrieves the winner.
             */
            storageService.delete(storedFilename);

            return receiptRepository.findByFileHash(fileHash)
                    .map(this::toResponse)
                    .orElseThrow(() -> ex);
        }
    }

    private void validateFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new InvalidReceiptFileException(
                    "Receipt file must not be empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidReceiptFileException(
                    "Receipt file must not exceed 10 MB");
        }

        String filename = safeOriginalFilename(file);

        String extension = getExtension(filename);

        boolean supported = false;

        for (String allowed : ALLOWED_EXTENSIONS) {
            if (allowed.equalsIgnoreCase(extension)) {
                supported = true;
                break;
            }
        }

        if (!supported) {
            throw new InvalidReceiptFileException(
                    "Unsupported receipt file type. " +
                            "Supported types: PDF, PNG, JPG, JPEG, TXT");
        }
    }

    private String safeOriginalFilename(MultipartFile file) {

        String filename = file.getOriginalFilename();

        if (filename == null || filename.isBlank()) {
            return "receipt";
        }

        /*
         * Prevent a filename such as:
         *
         * ../../some-file
         *
         * from being persisted as-is.
         */
        return Paths.get(filename)
                .getFileName()
                .toString();
    }

    private String getExtension(String filename) {

        int dotIndex = filename.lastIndexOf('.');

        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            return "";
        }

        return filename.substring(dotIndex + 1);
    }

    private ReceiptUploadResponse toResponse(Receipt receipt) {

        return new ReceiptUploadResponse(
                receipt.getId(),
                receipt.isProcessed(),
                receipt.getUploadedAt());
    }
}
