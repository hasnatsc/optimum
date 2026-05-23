package com.hasnat.optimum.setup.entity;

import com.hasnat.optimum.common.entity.BaseEntity;
import com.hasnat.optimum.organization.entity.Organization;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "stp_banks")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Bank extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)   private String bankCode;
    @Column(nullable = false, length = 200)  private String bankName;
    @Column(length = 200)                    private String bankNameLocal;
    @Column(length = 50)                     private String shortName;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30)
    private BankType bankType;

    @Enumerated(EnumType.STRING) @Column(length = 30)
    private BankCategory bankCategory;

    @Enumerated(EnumType.STRING) @Column(length = 20)
    private BankRating rating;

    @Column(length = 11)  private String swiftCode;
    @Column(length = 20)  private String centralBankCode;
    @Column(length = 9)   private String routingNumberPrefix;
    @Column(length = 500) private String headOfficeAddress;
    @Column(length = 100) private String headOfficeCity;
    @Column(length = 100) private String headOfficeCountry;
    @Column(length = 100) private String headOfficeEmail;
    @Column(length = 50)  private String headOfficePhone;
    @Column(length = 200) private String website;
    @Column(length = 200) private String onlineBankingUrl;
    @Column(length = 200) private String apiEndpoint;
    @Column(length = 50)  private String merchantId;
    @Column(length = 50)  private String correspondentBankName;
    @Column(length = 50)  private String correspondentAccountNumber;
    @Column(length = 11)  private String correspondentSwiftCode;

    @Column(nullable = false) private boolean isActive;
    @Column(nullable = false) private boolean supportsLc;
    @Column(nullable = false) private boolean supportsImportLc;
    @Column(nullable = false) private boolean supportsExportLc;
    @Column(nullable = false) private boolean supportsBtbLc;
    @Column(nullable = false) private boolean supportsInlandLc;
    @Column(nullable = false) private boolean supportsOnlineBanking;
    @Column(nullable = false) private boolean supportsApiIntegration;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    public enum BankType { COMMERCIAL, STATE_OWNED, PRIVATE, FOREIGN, SPECIALIZED, ISLAMIC, DEVELOPMENT, COOPERATIVE }
    public enum BankCategory { SCHEDULED, NON_SCHEDULED, MICROFINANCE, NBFI }
    public enum BankRating { UNRATED, EXCELLENT, GOOD, AVERAGE, POOR, BLACKLISTED }
}
