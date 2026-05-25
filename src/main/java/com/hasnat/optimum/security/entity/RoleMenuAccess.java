package com.hasnat.optimum.security.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Explicit mapping between a Role and an AppMenu item with per-action flags.
 *
 * When a role has at least one entry in this table with canView = true, the
 * user-menu endpoint uses ONLY this table to determine menu visibility for
 * that role — bypassing AppMenu.requiredPermission.
 *
 * When a role has NO canView = true entries here, the system falls back to
 * AppMenu.requiredPermission filtering (backward-compatible default).
 *
 * CRUD flags usage:
 *   canView   — controls whether the menu ITEM is visible in the navbar
 *   canCreate — frontend uses to show/hide Create buttons on that page
 *   canEdit   — frontend uses to show/hide Edit buttons
 *   canDelete — frontend uses to show/hide Delete buttons
 *
 * Updated from user's original:
 *   • menu field type changed: Menu → AppMenu (Menu.java removed as redundant)
 *   • @UniqueConstraint added to prevent duplicate role-menu pairs
 *   • Table kept: sec_mrole_menus (existing table name preserved)
 *
 * Table: sec_mrole_menus
 */
@Entity
@Table(
    name = "sec_mrole_menus",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_rma_role_menu",
        columnNames = { "role_id", "menu_id" }
    ),
    indexes = {
        @Index(name = "idx_rma_role_id", columnList = "role_id"),
        @Index(name = "idx_rma_menu_id", columnList = "menu_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleMenuAccess implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The role being granted access. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    /**
     * The AppMenu item this entry applies to.
     * Changed from old Menu to AppMenu — Menu.java is removed as redundant.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "menu_id", nullable = false)
    private AppMenu menu;

    // ── Per-action permission flags ───────────────────────────────────────────

    /** Controls navbar visibility — the menu item only appears if this is true. */
    @Builder.Default
    @Column(name = "can_view", nullable = false)
    private boolean canView = true;

    /** Frontend hint — show Create / Add buttons on the target page. */
    @Builder.Default
    @Column(name = "can_create", nullable = false)
    private boolean canCreate = false;

    /** Frontend hint — show Edit buttons on the target page. */
    @Builder.Default
    @Column(name = "can_edit", nullable = false)
    private boolean canEdit = false;

    /** Frontend hint — show Delete buttons on the target page. */
    @Builder.Default
    @Column(name = "can_delete", nullable = false)
    private boolean canDelete = false;

    // ── Audit ─────────────────────────────────────────────────────────────────

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() { createdAt = updatedAt = LocalDateTime.now(); }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}
