package com.hasnat.optimum.commercial.entity;

import com.hasnat.optimum.global.entity.BusinessDocument;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * FIXED: document_id FK now references BusinessDocument (was gbl_order_management).
 */
@Entity @Table(name = "com_lc_settlement")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LcSettlement {

    @Id private Long id;

    private LocalDate settlementDate;

    @Enumerated(EnumType.STRING) @Column(length = 20)
    private SettlementType settlementType;

    @Enumerated(EnumType.STRING) @Column(length = 20)
    private SettlementStatus status;

    @Column(precision = 18, scale = 4) private BigDecimal exchangeRate;
    @Column(precision = 18, scale = 2) private BigDecimal amountUsd;
    @Column(precision = 18, scale = 2) private BigDecimal amountBdt;
    @Column(precision = 18, scale = 2) private BigDecimal loanAmount;
    @Column(precision = 18, scale = 2) private BigDecimal marginUsed;
    @Column(precision = 18, scale = 2) private BigDecimal commission;
    @Column(precision = 18, scale = 2) private BigDecimal interest;
    @Column(precision = 18, scale = 2) private BigDecimal charges;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    private BusinessDocument document; // FIXED: was gbl_order_management

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lc_id")
    private LetterOfCredit lc;

    public enum SettlementType { SIGHT, USANCE, LOAN_ADJUSTMENT }
    public enum SettlementStatus { PENDING, PARTIAL, SETTLED, REVERSED }
}
