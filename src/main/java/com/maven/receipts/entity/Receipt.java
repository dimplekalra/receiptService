package com.maven.receipts.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "receipts", indexes = {
        @Index(name = "idx_receipt_processed", columnList = "processed")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_receipt_file_hash", columnNames = "file_hash")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Receipt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * SHA-256 hash of uploaded file.
     * Prevents duplicate uploads.
     */
    @Column(name = "file_hash", nullable = false, unique = true, length = 64)
    private String fileHash;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "stored_filename", nullable = false)
    private String storedFilename;

    @Column(name = "storage_path", nullable = false)
    private String storagePath;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private Instant uploadedAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean processed = false;

    @Lob
    @Column(name = "ocr_text")
    private String ocrText;

    @PrePersist
    public void prePersist() {
        this.uploadedAt = Instant.now();
    }
}
