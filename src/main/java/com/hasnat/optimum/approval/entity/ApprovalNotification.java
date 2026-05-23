package com.hasnat.optimum.approval.entity;

import com.hasnat.optimum.organization.entity.Organization;
import com.hasnat.optimum.security.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "apr_notifications")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ApprovalNotification {

    @Id private Long id;

    @Column(nullable = false, length = 200) private String subject;
    @Column(length = 500)                   private String shortMessage;
    @Column(columnDefinition = "text")      private String body;
    @Column(columnDefinition = "text")      private String htmlBody;
    @Column(length = 255)                   private String link;
    @Column(length = 200)                   private String recipientEmail;
    @Column(length = 50)                    private String recipientPhone;
    @Column(length = 500)                   private String notificationReason;
    @Column(length = 500)                   private String failureReason;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private NotificationType notificationType;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30)
    private NotificationReason reason;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private DeliveryStatus deliveryStatus;

    @Column(nullable = false) private boolean isRead;
    @Column(nullable = false) private boolean isClicked;
    @Column(nullable = false) private int retryCount;

    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime readAt;
    private LocalDateTime clickedAt;
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "approval_request_id", nullable = false)
    private ApprovalRequest approvalRequest;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    public enum NotificationType { EMAIL, SMS, IN_APP, PUSH, WHATSAPP }
    public enum DeliveryStatus {
        PENDING, READY, DISPATCHED, IN_TRANSIT, DELIVERED, PARTIAL,
        PARTIALLY_RETURNED, RETURNED, FAILED, BOUNCED, CANCELLED
    }
    public enum NotificationReason {
        NEW_APPROVAL_REQUEST, APPROVAL_PENDING, APPROVAL_REMINDER, REQUEST_APPROVED,
        REQUEST_REJECTED, REQUEST_RETURNED, REQUEST_ESCALATED, REQUEST_DELEGATED,
        REQUEST_FORWARDED, REQUEST_CANCELLED, DEADLINE_APPROACHING,
        DELEGATION_RECEIVED, DELEGATION_EXPIRED, COMMENT_ADDED, INFO_REQUESTED
    }
}
