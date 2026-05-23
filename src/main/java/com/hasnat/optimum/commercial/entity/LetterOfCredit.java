package com.hasnat.optimum.commercial.entity;

import com.hasnat.optimum.accounts.entity.BankAccount;
import com.hasnat.optimum.accounts.entity.ChartOfAccount;
import com.hasnat.optimum.accounts.entity.ChartOfAccountSub;
import com.hasnat.optimum.common.enums.Currency;
import com.hasnat.optimum.setup.entity.Bank;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * LetterOfCredit extends ChartOfAccountSub (SINGLE_TABLE inheritance).
 * The LC is itself registered as a sub-ledger entry under the LC control account.
 * FIXED: gbl_order_management references removed — settlements/mappings now reference BusinessDocument.
 */
@Entity
@DiscriminatorValue("LC")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class LetterOfCredit extends ChartOfAccountSub {

    @Column(nullable = false, unique = true, length = 100)
    private String lcNumber;

    @Column(length = 100) private String manualLcNumber;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30)
    private LcType lcType;

    @Enumerated(EnumType.STRING) @Column(length = 30)
    private LcStatus status;

    @Enumerated(EnumType.STRING) @Column(length = 20)
    private Currency transactionCurrency;

    @Column(precision = 18, scale = 2) private BigDecimal lcAmount;
    @Column(precision = 18, scale = 4) private BigDecimal exchangeRate;

    private LocalDate issueDate;
    private LocalDate expiryDate;
    private LocalDate shipmentDate;
    private LocalDate receivingDate;

    private Integer tenureDays;

    @Column(length = 100) private String masterLcNo;
    @Column(length = 100) private String btbLcNo;
    private LocalDate masterLcDate;

    @Enumerated(EnumType.STRING) @Column(length = 30)
    private PaymentTerm paymentTerm;

    @Enumerated(EnumType.STRING) @Column(length = 20)
    private ShipmentMode shipmentMode;

    private Boolean partialShipmentAllowed;
    private Boolean btmaCertificateRequired;

    @Column(columnDefinition = "text") private String termsCondition;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "bank_account_ledger_id")
    private ChartOfAccount bankAccountLedger;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "beneficiary_bank_id")
    private Bank beneficiaryBank;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "beneficiary_bank_account_id")
    private BankAccount beneficiaryBankAccount;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "buyer_bank_id")
    private Bank buyerBank;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "buyer_bank_account_id")
    private BankAccount buyerBankAccount;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "foreign_bank_id")
    private Bank foreignBank;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "margin_account_id")
    private ChartOfAccount marginAccount;

    // Stub FKs — full entities in Sales/Purchase modules
    @Column(name = "customer_id") private Long customerId;
    @Column(name = "supplier_id") private Long supplierId;

    public enum LcType { IMPORT, EXPORT, BACK_TO_BACK_EXPORT, BACK_TO_BACK_IMPORT }
    public enum LcStatus { DRAFT, OPENED, PARTIAL, UTILIZED, CLOSED, CANCELLED }
    public enum PaymentTerm {
        AT_SIGHT, USANCE_30_DAYS, USANCE_60_DAYS, USANCE_90_DAYS,
        USANCE_120_DAYS, USANCE_180_DAYS, DEFERRED_PAYMENT
    }
    public enum ShipmentMode { SEA, AIR, ROAD, RAIL, MULTIMODAL }
}
