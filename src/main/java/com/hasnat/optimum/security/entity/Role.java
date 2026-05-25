package com.hasnat.optimum.security.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A role groups a MasterRole authority with a set of fine-grained permissions.
 *
 * One role = one MasterRole + N permissions.
 * One user can have multiple roles (rare — usually just one).
 */
@Entity
@Table(
    name = "sec_roles",
    uniqueConstraints = @UniqueConstraint(name = "uq_role_master", columnNames = "master_role")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Human-readable display name. e.g. "Super Administrator" */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * The single Spring Security ROLE_* authority this role represents.
     * Stored as the enum name string in the DB column.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "master_role", nullable = false, unique = true, length = 50)
    private MasterRole masterRole;

    /** Optional description shown in the role management UI. */
    @Column(columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    /**
     * Fine-grained permissions assigned to this role.
     * Fetched eagerly so Spring Security can build the authority list
     * in a single transaction without N+1 issues.
     */
    @Builder.Default
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "sec_role_permissions",
        joinColumns        = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions = new LinkedHashSet<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
