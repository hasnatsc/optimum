package com.hasnat.optimum.accounts.entity;
import com.hasnat.optimum.common.entity.BaseEntity;
import com.hasnat.optimum.common.enums.ControlAccountType;
import com.hasnat.optimum.common.enums.ModuleType;
import com.hasnat.optimum.common.enums.TransactionType;
import com.hasnat.optimum.common.enums.VoucherType;
import com.hasnat.optimum.organization.entity.Organization;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "acc_mapping")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AccountMapping extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)  private String mappingCode;
    @Column(nullable = false, length = 200) private String mappingName;
    @Column(length = 500)                   private String description;
    @Column(length = 20)                    private String voucherPrefix;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30)
    private ModuleType moduleType;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 50)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING) @Column(length = 30)
    private VoucherType voucherType;

    @Enumerated(EnumType.STRING) @Column(length = 30)
    private VoucherType defaultVoucherType;

    // Defaults to NONE — never null (schema fix applied)
    @Enumerated(EnumType.STRING) @Column(length = 30, nullable = false)
    @Builder.Default private ControlAccountType debitControlType = ControlAccountType.NONE;

    @Enumerated(EnumType.STRING) @Column(length = 30, nullable = false)
    @Builder.Default private ControlAccountType creditControlType = ControlAccountType.NONE;

    @Column(length = 500) private String defaultNarrationTemplate;

    @Column(nullable = false) private boolean isActive;
    @Column(nullable = false) private boolean isDefault;
    @Column(nullable = false) private boolean isSystem;
    @Column(nullable = false) private boolean autoPost;
    @Column(nullable = false) private boolean requireApproval;
    @Column(nullable = false) private boolean useSubLedger;
    @Column(nullable = false) private boolean updateSubAccountBalance;
    @Column(nullable = false) private boolean allowPartialPosting;
    @Column(nullable = false) private boolean consolidateEntries;
    @Column(nullable = false) private boolean createReversingEntry;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "default_debit_account_id")
    private ChartOfAccount defaultDebitAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "default_credit_account_id")
    private ChartOfAccount defaultCreditAccount;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "discount_account_id")
    private ChartOfAccount discountAccount;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "freight_account_id")
    private ChartOfAccount freightAccount;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "input_vat_account_id")
    private ChartOfAccount inputVatAccount;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "output_vat_account_id")
    private ChartOfAccount outputVatAccount;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "tds_account_id")
    private ChartOfAccount tdsAccount;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "ait_account_id")
    private ChartOfAccount aitAccount;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "forex_gain_account_id")
    private ChartOfAccount forexGainAccount;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "forex_loss_account_id")
    private ChartOfAccount forexLossAccount;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "rounding_account_id")
    private ChartOfAccount roundingAccount;
}
