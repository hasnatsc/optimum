package com.hasnat.optimum.organization.entity;

import com.hasnat.optimum.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "org_business_units")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BusinessUnit extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private boolean isActive;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;
}
