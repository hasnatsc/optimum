package com.hasnat.optimum.hrm.entity;

import com.hasnat.optimum.common.entity.BaseEntity;
import com.hasnat.optimum.organization.entity.Organization;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "hrm_designations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Designation extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)  private String designationCode;
    @Column(nullable = false, length = 200) private String designationName;
    @Column(length = 20)                    private String grade;
    @Column(length = 500)                   private String description;
    @Column(nullable = false)               private boolean isActive;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;
}
