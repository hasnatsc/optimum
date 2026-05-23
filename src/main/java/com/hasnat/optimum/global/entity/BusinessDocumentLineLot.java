package com.hasnat.optimum.global.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity @Table(name = "global_business_document_line_lots")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BusinessDocumentLineLot {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 18, scale = 3) private BigDecimal quantity;
    private Integer bags;
    private Integer bagQuantity;
    private Integer conesPerBag;
    private Integer coneQuantity;
    @Column(precision = 12, scale = 3) private BigDecimal baleQuantity;
    @Column(precision = 12, scale = 3) private BigDecimal bagWeight;
    @Column(precision = 12, scale = 3) private BigDecimal netWeight;
    @Column(precision = 12, scale = 3) private BigDecimal actualWeight;
    @Column(precision = 18, scale = 4) private BigDecimal unitCost;
    @Column(precision = 18, scale = 2) private BigDecimal totalCost;

    @Enumerated(EnumType.STRING) @Column(length = 20)
    private BagCapacity bagCapacity;

    @Column(columnDefinition = "text") private String qualityRemarks;
    @Column(columnDefinition = "text") private String remarks;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_line_id", nullable = false)
    private BusinessDocumentLine documentLine;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lot_id", nullable = false)
    private InventoryLot lot;

    public enum BagCapacity { KG_5, KG_10, KG_20, KG_25, KG_50, KG_100, CUSTOM }
}
