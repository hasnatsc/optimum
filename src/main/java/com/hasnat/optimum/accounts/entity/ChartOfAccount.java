package com.hasnat.optimum.accounts.entity;

import com.hasnat.optimum.common.entity.BaseEntity;
import com.hasnat.optimum.common.enums.AccountNature;
import com.hasnat.optimum.common.enums.AccountType;
import com.hasnat.optimum.organization.entity.Organization;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity @Table(name = "acc_chart_of_accounts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ChartOfAccount extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String accountCode;

    @Column(nullable = false, length = 200)
    private String accountName;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private AccountNature accountNature;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private AccountType accountType;

    @Column(nullable = false) private int level;
    @Column(nullable = false) private boolean allowManualEntry;
    @Column(nullable = false) private boolean isControlAccount;
    @Column(nullable = false) private boolean isSystem;
    @Column(nullable = false) private boolean isActive;

    @Column(length = 10)   private String currency;
    @Column(length = 50)   private String taxId;
    @Column(length = 1000) private String description;

    @Column(precision = 18, scale = 2) private BigDecimal openingBalance;
    @Column(precision = 18, scale = 2) private BigDecimal currentBalance;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_account_id")
    private ChartOfAccount parentAccount;
}
