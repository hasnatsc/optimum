package com.hasnat.optimum.organization.entity;

import jakarta.persistence.*;
import lombok.*;
import com.hasnat.optimum.common.entity.BaseEntity;

@Entity @Table(name = "org_cost_centers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CostCenter extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String costCenterCode;

    @Column(nullable = false, length = 200)
    private String costCenterName;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private CostCenterType costCenterType;

    @Column(length = 1000) private String description;
    @Column(length = 100)  private String managerName;
    @Column(length = 100)  private String managerEmail;
    @Column(nullable = false) private boolean isActive;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_unit_id", nullable = false)
    private BusinessUnit businessUnit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_cost_center_id")
    private CostCenter parentCostCenter;

    public enum CostCenterType { DEPARTMENT, PROJECT, BRANCH, DIVISION, PRODUCT, SERVICE }
}
