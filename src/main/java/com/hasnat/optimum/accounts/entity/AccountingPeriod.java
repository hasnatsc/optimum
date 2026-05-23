package com.hasnat.optimum.accounts.entity;

import com.hasnat.optimum.common.entity.BaseEntity;
import com.hasnat.optimum.organization.entity.Organization;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity @Table(name = "acc_periods")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AccountingPeriod extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String periodName;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private PeriodType periodType;

    @Column(nullable = false) private int fiscalYear;
    @Column(nullable = false) private LocalDate startDate;
    @Column(nullable = false) private LocalDate endDate;
    @Column(nullable = false) private boolean isActive;
    @Column(nullable = false) private boolean isClosed;

    @Column(length = 1000) private String description;
    @Column(length = 100)  private String closedBy;
    private LocalDate closedDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    public enum PeriodType { DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY, CUSTOM }
}
