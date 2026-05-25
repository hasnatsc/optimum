package com.hasnat.optimum.security.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Application user entity.
 *
 * Table: sec_users
 *
 * UserDetails flags are stored as individual columns so they can be
 * toggled independently without reissuing credentials.
 *
 * Soft-delete: set deleted = true; never physically remove rows.
 */
@Entity
@Table(
    name = "sec_users",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_user_username", columnNames = "username"),
        @UniqueConstraint(name = "uq_user_email",    columnNames = "email")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    // ── Identity ──────────────────────────────────────────────────────────────

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Login identifier — also accepted as the "username" by Spring Security. */
    @Column(nullable = false, unique = true, length = 80)
    private String username;

    /** Used for login and for password-reset emails. Must be unique. */
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    /** Optional phone — accepted as alternative login principal. */
    @Column(length = 30)
    private String phone;

    /** Display name shown in the UI navbar. */
    @Column(name = "full_name", length = 200)
    private String fullName;

    /** BCrypt-encoded password. Never store plain text. */
    @Column(nullable = false)
    private String password;

    // ── UserDetails flags ─────────────────────────────────────────────────────

    @Builder.Default
    @Column(name = "is_enabled", nullable = false)
    private boolean enabled = true;

    @Builder.Default
    @Column(name = "is_account_non_expired", nullable = false)
    private boolean accountNonExpired = true;

    @Builder.Default
    @Column(name = "is_account_non_locked", nullable = false)
    private boolean accountNonLocked = true;

    @Builder.Default
    @Column(name = "is_credentials_non_expired", nullable = false)
    private boolean credentialsNonExpired = true;

    // ── Preferences ───────────────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(name = "default_dashboard", length = 30)
    private DefaultDashboard defaultDashboard;

    // ── Roles ─────────────────────────────────────────────────────────────────

    /**
     * EAGER fetch is intentional — Spring Security builds the authority list
     * on every request; lazy loading would require an open session.
     * Keep role count low (typically 1 per user).
     */
    @Builder.Default
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "sec_user_roles",
        joinColumns        = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new LinkedHashSet<>();

    // ── Soft-delete ───────────────────────────────────────────────────────────

    @Builder.Default
    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    // ── Audit ─────────────────────────────────────────────────────────────────

    @Column(name = "created_by",  length = 100)
    private String createdBy;

    @Column(name = "updated_by",  length = 100)
    private String updatedBy;

    @Column(name = "created_at",  updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ── Default dashboard enum ────────────────────────────────────────────────

    public enum DefaultDashboard {
        DEFAULT,
        PRODUCTION,
        INVENTORY,
        COMMERCIAL,
        HR,
        FINANCE
    }
}
