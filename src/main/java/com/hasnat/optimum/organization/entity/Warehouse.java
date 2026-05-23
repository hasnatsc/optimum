package com.hasnat.optimum.organization.entity;

import com.hasnat.optimum.common.enums.ItemType;
import jakarta.persistence.*;
import lombok.*;
import com.hasnat.optimum.common.entity.BaseEntity;

@Entity @Table(name = "org_warehouses")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Warehouse extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String warehouseCode;

    @Column(nullable = false, length = 200)
    private String warehouseName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ItemType itemType;

    @Column(columnDefinition = "text") private String address;
    @Column(length = 20)  private String contactNumber;
    @Column(length = 100) private String managerName;
    @Column(nullable = false) private boolean isActive;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_unit_id", nullable = false)
    private BusinessUnit businessUnit;
}
