package com.hasnat.optimum.accounts.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@DiscriminatorValue("CASH")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CashAccount extends ChartOfAccountSub {

    @Column(nullable = false, unique = true, length = 50)
    private String cashAccountCode;

    @Column(length = 200) private String accountTitle;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private CashAccountType cashAccountType;

    @Column(length = 100) private String custodian;
    @Column(length = 100) private String custodianEmail;
    @Column(length = 20)  private String custodianPhone;
    @Column(length = 100) private String location;
    @Column(length = 1000)private String remarks;

    @Column(nullable = false) private boolean requiresApproval;

    @Column(precision = 18, scale = 2) private BigDecimal minimumLimit;
    @Column(precision = 18, scale = 2) private BigDecimal maximumLimit;
    @Column(precision = 18, scale = 2) private BigDecimal approvalLimit;

    public enum CashAccountType { MAIN_CASH, PETTY_CASH, CASH_IN_HAND, CASH_DRAWER, IMPREST }
}
