package com.maven.receipts.repository;

import com.maven.receipts.entity.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReceiptRepository extends JpaRepository<Receipt, Long> {

    Optional<Receipt> findByFileHash(String fileHash);

}
