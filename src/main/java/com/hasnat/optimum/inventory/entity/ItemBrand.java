package com.hasnat.optimum.inventory.entity;

import com.hasnat.optimum.common.entity.BaseEntity;
import com.hasnat.optimum.organization.entity.Organization;
import com.hasnat.optimum.setup.entity.LocationCountry;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "inv_item_brands")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ItemBrand extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)   private String brandCode;
    @Column(nullable = false, length = 150)  private String brandName;
    @Column(length = 50)                     private String brandNameShort;
    @Column(columnDefinition = "text")       private String description;
    @Column(nullable = false)                private boolean isActive;
    @Column(nullable = false)                private boolean isApproved;
    @Column(length = 100)                    private String approvedBy;
    private LocalDateTime approvedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_of_origin_id")
    private LocationCountry countryOfOrigin;
}
