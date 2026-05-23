package com.hasnat.optimum.security.entity;

import com.hasnat.optimum.organization.entity.*;
import jakarta.persistence.*;
import lombok.*;

/**
 * FIXED: Dropped 5 orphan columns (org_business_units_id, org_cost_centers_id,
 *        organizations_id, users_id, warehouses_id) — all were raw bigints with no FK.
 *        The FK-constrained columns below supersede them entirely.
 */
@Entity @Table(name = "user_context")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserContext {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    // Approval notification preferences
    @Column(length = 20)  private String approvalDefaultView;
    @Column(length = 20)  private String approvalNotificationFrequency;
    private Boolean approvalDesktopNotification;
    private Boolean approvalEmailEnabled;
    private Boolean approvalPushEnabled;
    private Boolean approvalSmsEnabled;
    private Boolean approvalWhatsappEnabled;
    private Boolean approvalSoundEnabled;
    private Boolean showApprovalBadge;
    private Integer approvalRefreshInterval;

    // Reference to last viewed notification — FK added (was raw bigint before)
    @Column(name = "last_viewed_notification_id")
    private Long lastViewedNotificationId;

    // Active session context (FK-constrained — the only context columns needed)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_unit_id")
    private BusinessUnit businessUnit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cost_center_id")
    private CostCenter costCenter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;
}
