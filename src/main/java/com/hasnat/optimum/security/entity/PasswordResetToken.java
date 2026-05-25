package com.hasnat.optimum.security.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Represents a one-time password-reset token sent to the user's email.
 *
 * Lifecycle:
 *  CREATED → email sent → user clicks link → token validated → password updated → USED
 *  CREATED → token not used within TTL → EXPIRED (checked at validation time)
 */
@Entity
@Table(
    name = "sec_password_reset_tokens",
    indexes = {
        @Index(name = "idx_prt_token",      columnList = "token"),
        @Index(name = "idx_prt_user",       columnList = "user_id"),
        @Index(name = "idx_prt_expires_at", columnList = "expires_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetToken  implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unique UUID token string — sent in the reset link URL. */
    @Column(nullable = false, unique = true, length = 100)
    private String token;

    /** Owner of the token. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** When the token expires (default: 1 hour after creation). */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Timestamp when the token was consumed.
     * NULL means it has never been used.
     */
    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Business methods
    // ─────────────────────────────────────────────────────────────────────────

    /** Returns true if the token is still within its validity window. */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /** Returns true if the token has already been consumed. */
    public boolean isUsed() {
        return usedAt != null;
    }

    /** Returns true if the token can still be redeemed. */
    public boolean isValid() {
        return !isExpired() && !isUsed();
    }

    /** Mark the token as consumed. */
    public void markUsed() {
        this.usedAt = LocalDateTime.now();
    }
}
