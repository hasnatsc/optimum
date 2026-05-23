package com.hasnat.optimum.accounts.entity;

import com.hasnat.optimum.common.entity.BaseEntity;
import com.hasnat.optimum.common.enums.ModuleType;
import com.hasnat.optimum.common.enums.VoucherType;
import com.hasnat.optimum.organization.entity.Organization;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity @Table(name = "acc_policy")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AccountPolicy extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)  private String policyCode;
    @Column(nullable = false, length = 200) private String policyName;
    @Column(length = 500)                   private String description;
    @Column(length = 20)                    private String voucherPrefix;
    @Column(length = 20)                    private String voucherSuffix;
    @Column(length = 10)                    private String fiscalYearFormat;
    @Column(length = 50)                    private String approvalWorkflowCode;
    @Column(length = 500)                   private String defaultNarrationTemplate;
    @Column(length = 1000)                  private String notificationRecipients;
    @Column(length = 1000)                  private String approvalThresholds;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30)
    private VoucherType policyType;

    @Enumerated(EnumType.STRING) @Column(length = 30)
    private ModuleType moduleType;

    @Enumerated(EnumType.STRING) @Column(length = 20)
    private NumberingReset numberingReset;

    @Column(nullable = false) private boolean isActive;
    @Column(nullable = false) private boolean isSystem;
    private Boolean isDefault;

    // Permissions
    private Boolean allowBackdating;
    private Boolean allowFutureDating;
    private Boolean allowDelete;
    private Boolean allowEdit;
    private Boolean allowEditAfterPost;
    private Boolean allowReversal;
    private Boolean allowNegativeAmount;
    private Boolean allowZeroAmount;
    private Boolean allowDirectPost;

    // Requirements
    private Boolean requireApproval;
    private Boolean requireNarration;
    private Boolean requireCostCenter;
    private Boolean requireProject;
    private Boolean requireReference;
    private Boolean requireAttachment;
    private Boolean requireBalancedEntry;
    private Boolean requireReversalApproval;
    private Boolean restrictToOpenPeriod;

    // Auto behaviours
    private Boolean autoNumbering;
    private Boolean autoPost;
    private Boolean autoPostOnApproval;
    private Boolean postOnApproval;
    private Boolean autoApproveBelow;
    private Boolean validateCostCenter;
    private Boolean validateProject;
    private Boolean includeFiscalYear;
    private Boolean includeMonth;
    private Boolean notifyOnCreate;
    private Boolean notifyOnApproval;
    private Boolean notifyOnPost;
    private Boolean notifyOnReject;

    private Integer aprLevels;
    private Integer numberPadding;
    private Integer nextVoucherNumber;
    private Integer backdatingDays;
    private Integer futureDatingDays;
    private Integer editDaysLimit;
    private Integer minNarrationLength;

    @Column(precision = 18, scale = 2) private BigDecimal minimumAmount;
    @Column(precision = 18, scale = 2) private BigDecimal maximumAmount;
    @Column(precision = 18, scale = 2) private BigDecimal approvalThreshold;
    @Column(precision = 18, scale = 2) private BigDecimal autoApproveLimit;
    private LocalDate lastResetDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accounts_mapping_id")
    private AccountMapping accountsMapping;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "default_debit_account_id")
    private ChartOfAccount defaultDebitAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "default_credit_account_id")
    private ChartOfAccount defaultCreditAccount;

    public enum NumberingReset { NEVER, DAILY, MONTHLY, QUARTERLY, FISCAL_YEAR, CALENDAR_YEAR }
}
