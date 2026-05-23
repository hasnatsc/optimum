package com.hasnat.optimum.global.entity;

import com.hasnat.optimum.common.enums.DocumentType;
import com.hasnat.optimum.inventory.entity.Item;
import com.hasnat.optimum.organization.entity.Warehouse;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity @Table(name = "global_inventory_transactions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InventoryTransaction {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate transactionDate;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 50)
    private DocumentType documentType;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 50)
    private MovementType movementType;

    @Column(nullable = false, precision = 18, scale = 3) private BigDecimal quantity;
    @Column(precision = 18, scale = 3) private BigDecimal bags;
    private Integer bagQuantity;
    private Integer conesPerBag;
    @Column(precision = 18, scale = 3) private BigDecimal cones;
    @Column(precision = 18, scale = 3) private BigDecimal baleQuantity;
    @Column(precision = 12, scale = 3) private BigDecimal netWeight;
    @Column(precision = 12, scale = 3) private BigDecimal actualWeight;
    @Column(precision = 18, scale = 4) private BigDecimal unitCost;
    @Column(precision = 18, scale = 2) private BigDecimal totalCost;
    @Column(precision = 18, scale = 3) private BigDecimal balanceAfter;
    @Column(length = 255) private String remarks;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_document_id", nullable = false)
    private BusinessDocument businessDocument;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id")
    private InventoryLot lot;

    public enum MovementType {
        PURCHASE_RECEIPT, PRODUCTION_RECEIPT, SALE_ISSUE, STORE_ISSUE,
        TRANSFER_IN, TRANSFER_OUT, ADJUSTMENT_IN, ADJUSTMENT_OUT,
        CUSTOMER_RETURN, SUPPLIER_RETURN, PRODUCTION_MATERIAL_ISSUE
    }
}
