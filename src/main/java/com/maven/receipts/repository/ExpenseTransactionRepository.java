package com.maven.receipts.repository;

import com.maven.receipts.entity.ExpenseTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExpenseTransactionRepository extends JpaRepository<ExpenseTransaction, Long> {

    Optional<ExpenseTransaction> findByReceiptId(Long receiptId);

}
