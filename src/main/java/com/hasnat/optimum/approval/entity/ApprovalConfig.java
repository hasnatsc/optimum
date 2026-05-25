package com.hasnat.optimum.approval.entity;

import com.hasnat.optimum.common.entity.BaseEntity;
import com.hasnat.optimum.common.enums.*;
import com.hasnat.optimum.security.entity.Module;
import com.hasnat.optimum.organization.entity.Organization;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity @Table(name = "apr_configs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ApprovalConfig extends BaseEntity {

    @Id private Long id;

    @Column(nullable = false, unique = true, length = 50) private String code;
    @Column(nullable = false, length = 200)               private String name;
    @Column(length = 1000)                                private String description;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 50)
    private ApprovalDocumentType documentType;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30)
    private Module module;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private FlowType flowType;

    @Column(nullable = false) private boolean isActive;
    @Column(nullable = false) private boolean enableReminders;
    @Column(nullable = false) private boolean useReportingHierarchy;

    private Integer priority;
    private Integer autoEscalationHours;
    private Integer reminderIntervalHours;

    @Column(precision = 18, scale = 2) private BigDecimal minAmount;
    @Column(precision = 18, scale = 2) private BigDecimal maxAmount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    public enum FlowType { SEQUENTIAL, PARALLEL }
}
