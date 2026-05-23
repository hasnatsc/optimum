package com.hasnat.optimum.global.entity;

import com.hasnat.optimum.common.entity.BaseEntity;
import com.hasnat.optimum.inventory.entity.Item;
import com.hasnat.optimum.organization.entity.Organization;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity @Table(name = "global_business_document_lines")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BusinessDocumentLine extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) private int lineNumber;
    @Column(length = 100)     private String itemCode;
    @Column(length = 500)     private String itemName;
    @Column(length = 1000)    private String description;
    @Column(length = 100)     private String sku;
    @Column(length = 100)     private String batchNumber;
    @Column(length = 20)      private String unitCode;

    @Column(nullable = false, precision = 18, scale = 3) private BigDecimal quantity;
    @Column(precision = 10)   private BigDecimal bags;
    @Column(precision = 12, scale = 2) private BigDecimal baleQuantity;
    @Column(precision = 18, scale = 3) private BigDecimal deliveredQuantity;
    @Column(precision = 18, scale = 3) private BigDecimal acceptedQuantity;
    @Column(precision = 18, scale = 3) private BigDecimal receivedQuantity;
    @Column(precision = 18, scale = 3) private BigDecimal rejectedQuantity;
    @Column(precision = 18, scale = 4) private BigDecimal unitPrice;
    @Column(precision = 18, scale = 2) private BigDecimal discountAmount;
    @Column(precision = 18, scale = 2) private BigDecimal taxAmount;
    @Column(precision = 18, scale = 2) private BigDecimal lineAmount;

    @Enumerated(EnumType.STRING) @Column(length = 30)
    private QualityStatus qualityStatus;

    @Column(columnDefinition = "text") private String qualityRemarks;
    @Column(columnDefinition = "text") private String remarks;
    private LocalDate expectedDeliveryDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false)
    private BusinessDocument document;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_lot_id")
    private InventoryLot inventoryLot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_line_id")
    private BusinessDocumentLine sourceLine;

    public enum QualityStatus { PENDING, PASSED, FAILED, PARTIAL }
}
