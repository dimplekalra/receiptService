package com.maven.receipts.entity;

import com.maven.receipts.enums.ItemizeStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "transactions", uniqueConstraints = {
        @UniqueConstraint(name = "uk_transaction_receipt", columnNames = "receipt_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * One receipt can generate only one transaction.
     *
     * Database uniqueness is critical here.
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receipt_id", nullable = false, unique = true)
    private Receipt receipt;

    @Column(nullable = false)
    private String merchant;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(nullable = false, length = 3)
    private String currency;

    /**
     * Never use double/float for money.
     */
    @Column(name = "grand_total", nullable = false, precision = 19, scale = 2)
    private BigDecimal grandTotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "itemize_status", nullable = false)
    @Builder.Default
    private ItemizeStatus itemizeStatus = ItemizeStatus.NEEDS_REVIEW;
}
