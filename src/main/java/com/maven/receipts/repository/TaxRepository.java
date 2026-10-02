package com.maven.receipts.repository;

import com.maven.receipts.entity.Tax;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaxRepository extends JpaRepository<Tax, Long> {

}
