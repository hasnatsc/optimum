package com.hasnat.optimum.approval.entity;

import com.hasnat.optimum.security.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "apr_histories")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ApprovalHistory {

    @Id private Long id;

    @Column(nullable = false, length = 150) private String actorName;
    @Column(nullable = false, length = 100) private String levelName;
    @Column(nullable = false)               private int levelNumber;
    @Column(length = 100)                   private String actorDesignation;
    @Column(length = 150)                   private String actorDepartment;
    @Column(length = 2000)                  private String comments;
    @Column(length = 1000)                  private String rejectionReason;
    @Column(length = 1000)                  private String returnReason;
    @Column(length = 50)                    private String ipAddress;
    @Column(length = 500)                   private String userAgent;
    @Column(length = 100)                   private String autoActionTrigger;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30)
    private HistoryAction action;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private HistoryStatus status;

    @Column(nullable = false) private boolean isAutoAction;
    private Long responseTimeMinutes;
    private LocalDateTime actionAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "approval_request_id", nullable = false)
    private ApprovalRequest approvalRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_user_id")
    private User actorUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approval_level_id")
    private ApprovalLevel approvalLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delegated_from_user_id")
    private User delegatedFromUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "forwarded_to_user_id")
    private User forwardedToUser;

    public enum HistoryAction { SUBMITTED, APPROVED, REJECTED, RETURNED, RECALLED, CANCELLED, FORWARDED }
    public enum HistoryStatus {
        PENDING, APPROVED, REJECTED, RETURNED, ESCALATED, SKIPPED, CANCELLED, ON_HOLD, DELEGATED
    }
}
