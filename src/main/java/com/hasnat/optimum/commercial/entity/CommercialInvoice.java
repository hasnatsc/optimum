package com.hasnat.optimum.commercial.entity;

import com.hasnat.optimum.accounts.entity.ChartOfAccountSub;
import com.hasnat.optimum.organization.entity.Organization;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * FIXED numeric precision: exchange_rate/total_amount/total_amount_bdt
 * were numeric(38,2) in original — corrected to industry-standard types.
 * FIXED: delivery_id, grn_id are stub Long columns (Sales/Purchase module not provided).
 */
@Entity @Table(name = "com_commercial_invoice")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CommercialInvoice {

    @Id private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String invoiceNo;

    private LocalDate invoiceDate;

    @Enumerated(EnumType.STRING) @Column(length = 20)
    private InvoiceType invoiceType;

    @Enumerated(EnumType.STRING) @Column(length = 20)
    private InvoiceStatus status;

    @Column(length = 10) private String currency;

    // FIXED: was numeric(38,2) — corrected precision
    @Column(precision = 18, scale = 4) private BigDecimal exchangeRate;
    @Column(precision = 18, scale = 2) private BigDecimal totalAmount;
    @Column(precision = 18, scale = 2) private BigDecimal totalAmountBdt;

    @Column(length = 255) private String incoterms;
    @Column(length = 255) private String portOfLoading;
    @Column(length = 255) private String portOfDischarge;
    @Column(length = 255) private String vesselName;
    @Column(length = 255) private String blNumber;
    @Column(length = 255) private String containerNo;
    @Column(columnDefinition = "text") private String remarks;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lc_id")
    private LetterOfCredit lc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "party_id")
    private ChartOfAccountSub party;

    // Stub FK columns — full entities in Sales/Purchase modules
    @Column(name = "delivery_id") private Long deliveryId;
    @Column(name = "grn_id")      private Long grnId;

    public enum InvoiceType { EXPORT, IMPORT }
    public enum InvoiceStatus { DRAFT, FINALIZED, POSTED, CANCELLED }
}
