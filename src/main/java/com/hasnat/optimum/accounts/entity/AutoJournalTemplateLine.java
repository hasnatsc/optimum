package com.hasnat.optimum.accounts.entity;

import com.hasnat.optimum.common.entity.BaseEntity;
import com.hasnat.optimum.common.enums.ControlAccountType;
import com.hasnat.optimum.common.enums.TaxType;
import com.hasnat.optimum.organization.entity.CostCenter;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity @Table(name = "acc_auto_journal_template_lines")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AutoJournalTemplateLine extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) private int lineNumber;
    @Column(nullable = false) private int sortOrder;
    @Column(length = 100)     private String lineName;
    @Column(length = 500)     private String lineDescription;
    @Column(length = 500)     private String lineNarration;
    @Column(length = 500)     private String narrationVariables;
    @Column(length = 50)      private String accountCodePattern;
    @Column(length = 100)     private String fieldReference;
    @Column(length = 500)     private String formula;
    @Column(length = 500)     private String condition;
    @Column(length = 100)     private String conditionField;
    @Column(length = 20)      private String conditionOperator;
    @Column(length = 200)     private String conditionValue;
    @Column(length = 20)      private String taxCode;
    @Column(length = 30)      private String defaultCostCenterCode;
    @Column(length = 30)      private String defaultProjectCode;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 10)
    private AccountMappingDetail.EntryType entryType;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30)
    private AmountMode amountMode;

    @Enumerated(EnumType.STRING) @Column(length = 30)
    private ControlAccountType controlAccountType;

    @Enumerated(EnumType.STRING) @Column(length = 20)
    private TaxType taxType;

    @Column(precision = 8,  scale = 4) private BigDecimal amountPercentage;
    @Column(precision = 8,  scale = 4) private BigDecimal taxRate;
    @Column(precision = 18, scale = 2) private BigDecimal fixedAmount;
    @Column(precision = 18, scale = 2) private BigDecimal minimumAmount;
    @Column(precision = 18, scale = 2) private BigDecimal maximumAmount;

    private Integer decimalPlaces;
    @Column(nullable = false) private boolean isActive;
    @Column(nullable = false) private boolean isOptional;
    @Column(nullable = false) private boolean isTaxLine;
    @Column(nullable = false) private boolean negateAmount;
    @Column(nullable = false) private boolean roundAmount;
    @Column(nullable = false) private boolean skipIfZero;
    @Column(nullable = false) private boolean inheritCostCenter;
    @Column(nullable = false) private boolean useCostCenter;
    @Column(nullable = false) private boolean useProject;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "auto_journal_template_id", nullable = false)
    private AutoJournalTemplate autoJournalTemplate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private ChartOfAccount account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "control_account_id")
    private ChartOfAccount controlAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cost_center_id")
    private CostCenter costCenter;

    public enum AmountMode {
        DOCUMENT_TOTAL, DOCUMENT_SUBTOTAL, TAX_AMOUNT, DISCOUNT_AMOUNT, SHIPPING_CHARGES,
        OTHER_CHARGES, LINE_ITEM_TOTAL, CUSTOM_FIELD, PERCENTAGE_OF_TOTAL,
        FIXED_AMOUNT, FORMULA, BALANCE_AMOUNT
    }
}
