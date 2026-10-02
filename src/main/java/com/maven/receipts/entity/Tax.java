package com.maven.receipts.entity;

import java.math.BigDecimal;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "taxes", indexes = {
        @Index(name = "idx_tax_transaction", columnList = "transaction_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tax {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Parent transaction.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transaction_id", nullable = false)
    private ExpenseTransaction transaction;

    /**
     * Example:
     * VAT
     * GST
     * State Tax
     */
    @Column(nullable = false)
    private String name;

    /**
     * Example:
     * 20
     * 18
     *
     * Nullable because some receipts
     * only mention tax amount.
     */
    @Column(precision = 5, scale = 2)
    private BigDecimal rate;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
}
