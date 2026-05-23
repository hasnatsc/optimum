package com.hasnat.optimum.inventory.entity;

import com.hasnat.optimum.commercial.entity.HsCode;
import com.hasnat.optimum.common.entity.BaseEntity;
import com.hasnat.optimum.common.enums.ItemType;
import com.hasnat.optimum.organization.entity.Organization;
import com.hasnat.optimum.setup.entity.LocationCountry;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name = "inv_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Item extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)  private String itemCode;
    @Column(nullable = false, length = 200) private String itemName;
    @Column(length = 200)                   private String itemNameBn;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30)
    private ItemType itemType;

    @Enumerated(EnumType.STRING) @Column(length = 30)
    private FiberType fiberType;

    @Column(length = 100) private String sku;
    @Column(length = 100) private String barcode;
    @Column(length = 100) private String grade;
    @Column(length = 100) private String manufacturer;
    @Column(length = 100) private String model;
    @Column(length = 100) private String serialNumber;
    @Column(length = 100) private String originName;
    @Column(length = 50)  private String casNumber;
    @Column(length = 50)  private String chemicalFormula;
    @Column(length = 100) private String safetyDataSheet;
    @Column(nullable = false, length = 20) private String unitOfMeasure;
    @Column(nullable = false, length = 20) private String purchaseUnitCode;
    @Column(nullable = false, length = 20) private String salesUnitCode;

    // fiber/yarn quality attributes
    @Column(precision = 8, scale = 2) private BigDecimal micronaire;
    @Column(precision = 8, scale = 2) private BigDecimal stapleLength;
    @Column(precision = 8, scale = 2) private BigDecimal strength;
    @Column(precision = 8, scale = 2) private BigDecimal moisture;
    @Column(precision = 5, scale = 2) private BigDecimal purity;
    @Column(precision = 5, scale = 2) private BigDecimal trash;
    @Column(precision = 8, scale = 2) private BigDecimal concentration;

    // stock management
    @Column(precision = 12, scale = 3) private BigDecimal minimumStock;
    @Column(precision = 12, scale = 3) private BigDecimal maximumStock;
    @Column(precision = 12, scale = 3) private BigDecimal reorderLevel;

    // pricing & cost
    @Column(precision = 12, scale = 4) private BigDecimal costPrice;
    @Column(precision = 12, scale = 4) private BigDecimal unitPrice;
    @Column(precision = 12, scale = 2) private BigDecimal standardCostPerKg;
    @Column(precision = 12, scale = 2) private BigDecimal sellingPricePerKg;
    @Column(precision = 15, scale = 2) private BigDecimal assetValue;
    @Column(precision = 5,  scale = 2) private BigDecimal taxRate;
    @Column(precision = 5,  scale = 2) private BigDecimal depreciationRate;

    // production
    @Column(precision = 5, scale = 2) private BigDecimal yieldPercent;
    @Column(precision = 5, scale = 2) private BigDecimal processLossPercent;

    private Integer warrantyMonths;
    private LocalDateTime expiryDate;

    @Column(nullable = false) private boolean isActive;
    @Column(nullable = false) private boolean isApproved;
    private Boolean isHazardous;
    @Column(length = 100)     private String approvedBy;
    private LocalDateTime approvedAt;

    @Column(columnDefinition = "text") private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private ItemCategory category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "purchase_unit_id", nullable = false)
    private ItemUom purchaseUnit;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sales_unit_id", nullable = false)
    private ItemUom salesUnit;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "operation_unit_id", nullable = false)
    private ItemUom operationUnit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hs_code_id")
    private HsCode hsCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_id")
    private LocationCountry origin;

    public enum FiberType {
        COTTON, LINEN, WOOL, SILK, VISCOSE, MODAL, LYOCELL, BAMBOO,
        POLYESTER, NYLON, ACRYLIC, POLYPROPYLENE, ELASTANE, ARAMID
    }
}
