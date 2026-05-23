package com.hasnat.optimum.accounts.entity;

import com.hasnat.optimum.common.entity.BaseEntity;
import com.hasnat.optimum.organization.entity.Organization;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity @Table(name = "acc_opening_balances")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OpeningBalance extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 18, scale = 2) private BigDecimal openingDebitBalance;
    @Column(nullable = false, precision = 18, scale = 2) private BigDecimal openingCreditBalance;
    @Column(nullable = false) private boolean isPosted;

    @Column(length = 255) private String balanceType;
    @Column(length = 100) private String postedBy;
    @Column(length = 1000)private String remarks;
    private LocalDate postedDate;
    private Boolean isActive;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private ChartOfAccount account;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "accounting_period_id", nullable = false)
    private AccountingPeriod accountingPeriod;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;
}
