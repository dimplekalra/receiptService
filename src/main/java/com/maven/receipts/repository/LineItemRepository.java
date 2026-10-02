package com.maven.receipts.repository;

import com.maven.receipts.entity.LineItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LineItemRepository extends JpaRepository<LineItem, Long> {

}
