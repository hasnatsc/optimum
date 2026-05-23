package com.hasnat.optimum.approval.entity;

import com.hasnat.optimum.common.entity.BaseEntity;
import com.hasnat.optimum.security.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "apr_levels")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ApprovalLevel extends BaseEntity {

    @Id private Long id;

    @Column(nullable = false, length = 100) private String levelName;
    @Column(nullable = false)               private int levelNumber;
    @Column(length = 500)                   private String description;
    @Column(length = 200)                   private String approverDescription;
    @Column(nullable = false)               private boolean isActive;
    @Column(nullable = false)               private boolean canDelegate;
    @Column(nullable = false)               private boolean canForward;
    @Column(nullable = false)               private boolean canHold;
    @Column(nullable = false)               private boolean canApproveWithChanges;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "approval_config_id", nullable = false)
    private ApprovalConfig approvalConfig;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_user_id")
    private User approverUser;
}
