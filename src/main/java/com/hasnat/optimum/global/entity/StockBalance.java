package com.hasnat.optimum.global.entity;

import com.hasnat.optimum.inventory.entity.Item;
import com.hasnat.optimum.organization.entity.Warehouse;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name = "global_inventory_stock_balances")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StockBalance {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 18, scale = 3) private BigDecimal quantity;
    private Integer bags;
    private Integer bagQuantity;
    private Integer conesPerBag;
    private Integer coneQuantity;
    @Column(precision = 12, scale = 3) private BigDecimal baleQuantity;
    @Column(precision = 12, scale = 3) private BigDecimal netWeight;
    @Column(precision = 12, scale = 3) private BigDecimal actualWeight;
    @Column(precision = 18, scale = 4) private BigDecimal averageCost;
    @Column(precision = 18, scale = 2) private BigDecimal stockValue;
    private LocalDateTime lastTransactionTime;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id")
    private InventoryLot lot;
}
