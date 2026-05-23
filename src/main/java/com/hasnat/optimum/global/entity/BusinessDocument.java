package com.hasnat.optimum.global.entity;

import com.hasnat.optimum.accounts.entity.ChartOfAccountSub;
import com.hasnat.optimum.approval.entity.ApprovalRequest;
import com.hasnat.optimum.common.entity.BaseEntity;
import com.hasnat.optimum.common.enums.*;
import com.hasnat.optimum.organization.entity.*;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Table(name = "global_business_documents")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BusinessDocument extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String documentNo;

    @Column(length = 100) private String documentNoManual;

    @Column(nullable = false) private LocalDate documentDate;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 50)
    private DocumentType documentType;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30)
    private DocumentStatus status;

    @Enumerated(EnumType.STRING) @Column(length = 30)
    private ApprovalStatus approvalStatus;

    @Enumerated(EnumType.STRING) @Column(length = 20)
    private ItemType itemType;

    @Enumerated(EnumType.STRING) @Column(length = 20)
    private Priority priority;

    // Financial
    @Column(length = 20)                   private String currency;
    @Column(precision = 18, scale = 4)     private BigDecimal exchangeRate;
    @Column(precision = 18, scale = 2)     private BigDecimal subtotalAmount;
    @Column(precision = 18, scale = 2)     private BigDecimal discountAmount;
    @Column(precision = 18, scale = 2)     private BigDecimal taxAmount;
    @Column(precision = 18, scale = 2)     private BigDecimal shippingAmount;
    @Column(precision = 18, scale = 2)     private BigDecimal totalAmount;
    @Column(precision = 18, scale = 2)     private BigDecimal paidAmount;
    @Column(precision = 18, scale = 2)     private BigDecimal dueAmount;
    @Column(nullable = false)              private boolean stockPosted;

    // Shipping / logistics
    @Column(length = 50)  private String incoterms;
    @Column(length = 50)  private String portOfLoading;
    @Column(length = 50)  private String portOfDischarge;
    @Column(length = 100) private String vesselName;
    @Column(length = 100) private String blNumber;
    @Column(length = 100) private String containerNumber;
    @Column(length = 100) private String challanNo;
    @Column(length = 100) private String vehicleNumber;
    @Column(length = 100) private String driverName;
    @Column(length = 100) private String exportLcNumber;

    // Dates
    private LocalDate deliveryDate;
    private LocalDate requiredDate;
    private LocalDate validityDate;

    // Contact
    @Column(length = 500) private String deliveryAddress;
    @Column(length = 100) private String contactPerson;
    @Column(length = 20)  private String contactNumber;

    // Misc
    @Column(length = 100)  private String referenceNo;
    @Column(columnDefinition = "text") private String termsAndConditions;
    @Column(columnDefinition = "text") private String remarks;

    // Soft-delete (added — was missing in original)
    @Column(nullable = false) @Builder.Default
    private boolean isDeleted = false;
    private LocalDateTime deletedAt;
    @Column(length = 100) private String deletedBy;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approval_request_id")
    private ApprovalRequest approvalRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_document_id")
    private BusinessDocument parentDocument;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "party_id")
    private ChartOfAccountSub party;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    public enum DocumentStatus {
        DRAFT, SUBMITTED, APPROVED, PROCESSING, PARTIAL, PARTIALLY_CONVERTED,
        COMPLETED, REJECTED, RETURNED, CANCELLED, CONVERTED
    }
    public enum ApprovalStatus {
        DRAFT, SUBMITTED, PENDING, IN_PROGRESS, IN_APPROVAL, APPROVED, REJECTED,
        RETURNED, CANCELLED, CLOSED, ON_HOLD, AUTO_APPROVED, ESCALATED, SKIPPED, DELEGATED
    }
}
