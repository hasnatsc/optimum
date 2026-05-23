package com.hasnat.optimum.accounts.entity;

import com.hasnat.optimum.common.entity.BaseEntity;
import com.hasnat.optimum.common.enums.ControlAccountType;
import com.hasnat.optimum.common.enums.TaxType;
import com.hasnat.optimum.organization.entity.CostCenter;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity @Table(name = "acc_mapping_details")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AccountMappingDetail extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) private int lineNumber;
    @Column(nullable = false) private int sortOrder;
    @Column(length = 100)     private String entryName;
    @Column(length = 500)     private String entryDescription;
    @Column(length = 500)     private String lineNarration;
    @Column(length = 50)      private String accountCodePattern;
    @Column(length = 20)      private String taxCode;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 10)
    private EntryType entryType;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30)
    private AmountType amountType;

    @Enumerated(EnumType.STRING) @Column(length = 30)
    private ControlAccountType controlAccountType;

    @Enumerated(EnumType.STRING) @Column(length = 20)
    private TaxType taxType;

    @Column(length = 500) private String condition;
    @Column(length = 100) private String conditionField;
    @Column(length = 20)  private String conditionOperator;
    @Column(length = 200) private String conditionValue;
    @Column(length = 100) private String fieldReference;
    @Column(length = 500) private String formula;

    @Column(precision = 8,  scale = 4) private BigDecimal percentage;
    @Column(precision = 8,  scale = 4) private BigDecimal taxRate;
    @Column(precision = 18, scale = 2) private BigDecimal fixedAmount;
    @Column(precision = 18, scale = 2) private BigDecimal minimumAmount;
    @Column(precision = 18, scale = 2) private BigDecimal maximumAmount;

    private Integer decimalPlaces;
    @Column(nullable = false) private boolean isActive;
    @Column(nullable = false) private boolean isOptional;
    @Column(nullable = false) private boolean isTaxEntry;
    @Column(nullable = false) private boolean negateAmount;
    @Column(nullable = false) private boolean roundAmount;
    @Column(nullable = false) private boolean skipIfZero;
    @Column(nullable = false) private boolean inheritCostCenter;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "accounts_mapping_id", nullable = false)
    private AccountMapping accountsMapping;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private ChartOfAccount account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cost_center_id")
    private CostCenter costCenter;

    public enum EntryType { DEBIT, CREDIT }
    public enum AmountType {
        FULL_AMOUNT, SUBTOTAL, TAX_ONLY, DISCOUNT_ONLY, FIXED_PERCENTAGE,
        FIXED_AMOUNT, FIELD_VALUE, FORMULA, BALANCE
    }
}
