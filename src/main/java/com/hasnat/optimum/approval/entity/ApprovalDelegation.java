package com.hasnat.optimum.approval.entity;

import com.hasnat.optimum.common.entity.BaseEntity;
import com.hasnat.optimum.common.enums.*;
import com.hasnat.optimum.security.entity.Module;
import com.hasnat.optimum.organization.entity.Organization;
import com.hasnat.optimum.security.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Table(name = "apr_delegations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ApprovalDelegation extends BaseEntity {

    @Id private Long id;

    @Column(nullable = false, unique = true, length = 50) private String delegationCode;
    @Column(length = 10)   private String currency;
    @Column(length = 1000) private String reason;
    @Column(length = 500)  private String revocationReason;

    @Enumerated(EnumType.STRING) @Column(length = 50)
    private ApprovalDocumentType documentType;

    @Enumerated(EnumType.STRING) @Column(length = 30)
    private Module module;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private DelegationStatus status;

    @Column(nullable = false) private LocalDate startDate;
    @Column(nullable = false) private LocalDate endDate;
    @Column(nullable = false) private boolean isActive;
    @Column(nullable = false) private boolean notifyDelegator;
    @Column(nullable = false) private boolean ccDelegator;

    @Column(precision = 18, scale = 2) private BigDecimal maxAmount;
    private LocalDateTime revokedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "delegator_id", nullable = false)
    private User delegator;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "delegate_id", nullable = false)
    private User delegate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "revoked_by_id")
    private User revokedBy;

    public enum DelegationStatus { SCHEDULED, ACTIVE, EXPIRED, REVOKED }
}
