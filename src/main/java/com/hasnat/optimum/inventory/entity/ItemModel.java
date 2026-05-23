package com.hasnat.optimum.inventory.entity;

import com.hasnat.optimum.common.entity.BaseEntity;
import com.hasnat.optimum.organization.entity.Organization;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "inv_item_models")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ItemModel extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)   private String modelCode;
    @Column(nullable = false, length = 150)  private String modelName;
    @Column(length = 50)                     private String modelNameShort;
    @Column(columnDefinition = "text")       private String description;
    @Column(nullable = false)                private boolean isActive;
    @Column(nullable = false)                private boolean isApproved;
    @Column(length = 100)                    private String approvedBy;
    private LocalDateTime approvedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private ItemBrand brand;
}
