package com.hasnat.optimum.inventory.entity;

import com.hasnat.optimum.common.entity.BaseEntity;
import com.hasnat.optimum.common.enums.ItemType;
import com.hasnat.optimum.organization.entity.Organization;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "inv_item_categories")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ItemCategory extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String categoryCode;

    @Column(nullable = false, length = 100)
    private String categoryName;

    @Enumerated(EnumType.STRING) @Column(length = 30)
    private ItemType itemType;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private LayerType layerType;

    @Column(columnDefinition = "text") private String description;
    @Column(nullable = false)          private boolean isActive;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id")
    private ItemCategory parentCategory;

    public enum LayerType { ROOT, GROUP, ITEM }
}
