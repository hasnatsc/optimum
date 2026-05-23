package com.hasnat.optimum.accounts.entity;

import com.hasnat.optimum.setup.entity.Bank;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@DiscriminatorValue("BANK")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class BankAccount extends ChartOfAccountSub {

    @Column(nullable = false, unique = true, length = 50)
    private String bankAccountCode;

    @Column(nullable = false, unique = true, length = 50)
    private String accountNumber;

    @Column(length = 200) private String accountTitle;
    @Column(length = 200) private String bankName;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private BankAccountType bankAccountType;

    @Column(nullable = false, length = 100) private String branchName;
    @Column(length = 10)  private String branchCode;
    @Column(length = 200) private String branchAddress;
    @Column(length = 20)  private String branchPhone;
    @Column(length = 34)  private String ibanNumber;
    @Column(length = 11)  private String swiftCode;
    @Column(length = 9)   private String routingNumber;
    @Column(length = 1000)private String remarks;

    @Column(precision = 8,  scale = 4) private BigDecimal interestRate;
    @Column(precision = 18, scale = 2) private BigDecimal overdraftLimit;
    @Column(precision = 8,  scale = 4) private BigDecimal overdraftInterestRate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_id")
    private Bank bank;

    public enum BankAccountType {
        SAVINGS, CURRENT, FIXED_DEPOSIT, RECURRING_DEPOSIT, LOAN,
        OVERDRAFT, CREDIT_CARD, NOSTRO, VOSTRO, ESCROW
    }
}
