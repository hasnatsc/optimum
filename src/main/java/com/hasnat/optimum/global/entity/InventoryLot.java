package com.hasnat.optimum.global.entity;

import com.hasnat.optimum.common.entity.BaseEntity;
import com.hasnat.optimum.common.enums.ItemType;
import com.hasnat.optimum.inventory.entity.Item;
import com.hasnat.optimum.organization.entity.Organization;
import com.hasnat.optimum.setup.entity.Bank;
import com.hasnat.optimum.setup.entity.LocationCountry;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * global_inv_lots
 * CHANGE: `active` column REMOVED (was redundant with `deleted` + `status`).
 * Use: deleted = false AND status = AVAILABLE to find usable lots.
 * `version` maps to JPA @Version for optimistic locking.
 */
@Entity @Table(name = "global_inv_lots")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InventoryLot extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** JPA optimistic locking — do not set manually. */
    @Version
    private Long version;

    @Column(nullable = false) @Builder.Default
    private boolean deleted = false;

    @Column(nullable = false, length = 100)
    private String lotNumber;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30)
    private ItemType itemType;

    @Enumerated(EnumType.STRING) @Column(length = 30)
    private LotStatus status;

    @Enumerated(EnumType.STRING) @Column(length = 50)
    private Certification certification;

    @Enumerated(EnumType.STRING) @Column(length = 50)
    private ColorGrade colorGrade;

    @Column(length = 100) private String batchNo;
    @Column(length = 100) private String manufacturerBatchNo;
    @Column(length = 100) private String binLocation;
    @Column(length = 100) private String shelfLocation;
    @Column(length = 100) private String warehouseLocation;
    @Column(length = 100) private String chemicalGrade;
    @Column(length = 250) private String bankName;

    // Quality attributes
    @Column(precision = 8, scale = 3) private BigDecimal micronaire;
    @Column(precision = 8, scale = 3) private BigDecimal avgMicronaire;
    @Column(precision = 8, scale = 3) private BigDecimal stapleLength;
    @Column(precision = 8, scale = 3) private BigDecimal avgStapleLength;
    @Column(precision = 8, scale = 3) private BigDecimal moisture;
    @Column(precision = 8, scale = 3) private BigDecimal avgMoisture;
    @Column(precision = 5, scale = 3) private BigDecimal purity;
    @Column(precision = 5, scale = 3) private BigDecimal avgPurity;
    @Column(precision = 5, scale = 3) private BigDecimal trashPercent;
    @Column(precision = 5, scale = 3) private BigDecimal avgTrashPercent;
    @Column(precision = 8, scale = 3) private BigDecimal denier;
    @Column(precision = 8, scale = 3) private BigDecimal concentration;

    private LocalDate manufacturingDate;
    private LocalDate productionDate;
    private LocalDate receivedDate;
    private LocalDate expiryDate;

    @Column(columnDefinition = "text") private String remarks;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_id")
    private Bank bank;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_of_origin_id")
    private LocationCountry countryOfOrigin;

    // Supplier & production recipe are stub references — FK only
    @Column(name = "supplier_id")         private Long supplierId;
    @Column(name = "production_recipe_id") private Long productionRecipeId;

    public enum LotStatus { AVAILABLE, RESERVED, BLOCKED, QC_HOLD, EXPIRED, CONSUMED }
    public enum Certification {
        BCI, ORGANIC_OCS, USCJP, PSCP, ORGANIC_GOTS, BCI_PHYSICAL, CMIA,
        RECYCLE_RCS, RECYCLE_GRS, SUPIMA, GIZA, ECOVERO_LIVA, ECOVERO_LENZING,
        FSC, EUROPEAN_FLAX
    }
    public enum ColorGrade { WHITE, LIGHT_SPOTTED, SPOTTED, YELLOW, BROWN, MIXED, OTHER }
}
