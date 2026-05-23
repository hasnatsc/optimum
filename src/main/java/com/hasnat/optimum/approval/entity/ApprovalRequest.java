package com.hasnat.optimum.approval.entity;

import com.hasnat.optimum.common.entity.BaseEntity;
import com.hasnat.optimum.common.enums.ApprovalDocumentType;
import com.hasnat.optimum.organization.entity.Organization;
import com.hasnat.optimum.security.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Table(name = "apr_requests")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ApprovalRequest extends BaseEntity {

    @Id private Long id;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 50)
    private ApprovalDocumentType documentType;

    @Column(nullable = false)               private Long referenceId;
    @Column(nullable = false, length = 100) private String referenceNumber;
    @Column(length = 500)                   private String documentSummary;
    @Column(length = 200)                   private String requesterName;
    @Column(length = 80)                    private String currentApproverRole;
    @Column(length = 1000)                  private String finalRemarks;
    @Column(length = 100)                   private String finalActionBy;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private RequestStatus status;

    @Column(nullable = false) private int currentLevelNumber;
    @Column(nullable = false) private int totalLevels;
    @Column(nullable = false) private boolean isUrgent;

    @Column(precision = 18, scale = 2) private BigDecimal documentAmount;
    private LocalDate documentDate;
    private LocalDate dueDate;
    private LocalDateTime completedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approval_config_id")
    private ApprovalConfig approvalConfig;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_approval_level_id")
    private ApprovalLevel currentApprovalLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_approver_user_id")
    private User currentApproverUser;

    public enum RequestStatus {
        DRAFT, SUBMITTED, IN_APPROVAL, APPROVED, REJECTED, RETURNED,
        CANCELLED, EXPIRED, COMPLETED, HOLD, CLOSED
    }
}
