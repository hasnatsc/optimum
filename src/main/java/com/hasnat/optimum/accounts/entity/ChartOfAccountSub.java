package com.hasnat.optimum.accounts.entity;

import com.hasnat.optimum.common.entity.BaseEntity;
import com.hasnat.optimum.common.enums.Currency;
import com.hasnat.optimum.organization.entity.Organization;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

/**
 * Single-table inheritance base for all sub-ledger account types.
 * Discriminator column: sub_account_type
 * Subtypes: BankAccount | CashAccount | Customer (sales) | Supplier (purchase)
 *           | LetterOfCredit (commercial)
 */
@Entity
@Table(name = "acc_chart_of_accounts_sub")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "sub_account_type", discriminatorType = DiscriminatorType.STRING, length = 31)
@Getter @Setter @NoArgsConstructor
public abstract class ChartOfAccountSub extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String subAccountCode;

    @Column(nullable = false, length = 200)
    private String subAccountName;

    @Enumerated(EnumType.STRING) @Column(length = 20)
    private Currency currency;

    @Column(nullable = false) private boolean isActive;

    @Column(length = 500) private String address;
    @Column(length = 50)  private String city;
    @Column(length = 50)  private String state;
    @Column(length = 50)  private String country;
    @Column(length = 20)  private String postalCode;
    @Column(length = 200) private String contactPerson;
    @Column(length = 20)  private String contactPhone;
    @Column(length = 100) private String contactEmail;
    @Column(length = 50)  private String taxId;
    @Column(length = 50)  private String vatRegistrationNo;
    @Column(length = 1000)private String description;

    @Column(precision = 18, scale = 2) private BigDecimal openingBalance;
    @Column(precision = 18, scale = 2) private BigDecimal currentBalance;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "main_account_id", nullable = false)
    private ChartOfAccount mainAccount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;
}
