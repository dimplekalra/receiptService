package com.maven.receipts.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "line_items", indexes = {
        @Index(name = "idx_item_transaction", columnList = "transaction_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class LineItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Parent transaction.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transaction_id", nullable = false)
    private ExpenseTransaction transaction;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    /**
     * Optional.
     * Some receipts don't mention quantity.
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal quantity;

    /**
     * Optional.
     */
    @Column(precision = 19, scale = 2)
    private BigDecimal taxAmount;
}
