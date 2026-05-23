package com.hasnat.optimum.inventory.entity;

import com.hasnat.optimum.organization.entity.Organization;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name = "inv_item_uom")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ItemUom {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)  private String code;
    @Column(nullable = false, length = 100) private String name;
    @Column(length = 20)                    private String symbol;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30)
    private UomCategory category;

    @Column(nullable = false, precision = 12, scale = 6)
    private BigDecimal conversionFactor;

    @Column(nullable = false) private boolean isBaseUnit;
    @Column(nullable = false) private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    public enum UomCategory { WEIGHT, COUNT, LENGTH, VOLUME, PACKING }
}
