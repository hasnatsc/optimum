package com.hasnat.optimum.organization.entity;

import jakarta.persistence.*;
import lombok.*;
import com.hasnat.optimum.common.entity.BaseEntity;

@Entity @Table(name = "org_departments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Department extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 50)
    private String code;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 500) private String description;
    @Column(nullable = false) private boolean active;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    // circular ref resolved after HRM module: headEmployee set via setter
    @Column(name = "head_employee_id")
    private Long headEmployeeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_department_id")
    private Department parentDepartment;
}
