package com.hasnat.optimum.security.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * A fine-grained permission authority.
 * Name format: PERM_{MODULE}_{ACTION}
 * Example: PERM_USER_CREATE, PERM_PRODUCTION_ORDER_APPROVE
 *
 * Permissions are assigned to Roles, not directly to Users.
 * Spring Security sees each name as a GrantedAuthority string.
 */
@Entity
@Table(
    name = "sec_permissions",
    uniqueConstraints = @UniqueConstraint(name = "uq_permission_name", columnNames = "name")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Authority name used in @PreAuthorize.
     * e.g. "PERM_USER_CREATE"
     */
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    /** Human-readable label shown in the role management UI. */
    @Column(length = 200)
    private String description;

    /**
     * Logical module this permission belongs to.
     * Used for grouping in the role assignment UI.
     * e.g. "USER_MANAGEMENT", "PRODUCTION", "INVENTORY"
     */
    @Column(length = 100)
    private String module;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
