package com.hasnat.optimum.security.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Single unified navigation menu entity.
 *
 * Replaces BOTH the old {@code Menu} (sec_menus) and the old {@code AppMenu}:
 *
 *  From old Menu     → menuCode, description, visible (was isVisible), moduleName
 *  From old AppMenu  → parentId (Long flat), menuType, deleted, createdBy/updatedBy
 *
 *  DROPPED from old Menu:
 *    • @ManyToOne parentMenu  — caused N+1 on every tree load.
 *                               Replaced by plain Long parentId.
 *    • Module enum            — external dep (com.hasnat.optimum.security.entity.Module).
 *                               Replaced by String moduleName.
 *
 * Tree is loaded in ONE query; JS buildMenuTree() reconstructs the hierarchy
 * from the flat parentId adjacency list.
 *
 * Visibility rules (all must be true for a menu item to appear):
 *   active  = true  (admin toggle)
 *   visible = true  (explicit show/hide without deactivating)
 *   deleted = false (soft-delete)
 *   + permission check via RoleMenuAccess.canView OR requiredPermission
 *
 * Table: app_menus
 */
@Entity
@Table(
    name = "app_menus",
    uniqueConstraints = @UniqueConstraint(name = "uq_app_menu_code", columnNames = "menu_code"),
    indexes = {
        @Index(name = "idx_menu_parent",  columnList = "parent_id"),
        @Index(name = "idx_menu_order",   columnList = "display_order"),
        @Index(name = "idx_menu_active",  columnList = "active, deleted")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppMenu implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Identity ──────────────────────────────────────────────────────────────

    /**
     * Short stable code for programmatic lookup (from old Menu.menuCode).
     * Survives renames — use this in code that must find a specific menu.
     * e.g. "PROD_ORDERS", "INV_ITEMS", "SEC_USERS"
     */
    @Column(name = "menu_code", nullable = false, unique = true, length = 80)
    private String menuCode;

    @Column(nullable = false, length = 120)
    private String menuName;

    /** Optional human-readable description (from old Menu.description). */
    @Column(length = 255)
    private String description;

    // ── Display ───────────────────────────────────────────────────────────────

    /**
     * Font-Awesome icon class — required for MODULE items.
     * e.g. "fa fa-industry", "fa fa-boxes-stacked"
     * Leave null for GROUP and LEAF.
     */
    @Column(length = 100)
    private String icon;

    /**
     * Navigation URL — required for LEAF items, null for MODULE/GROUP.
     * Must start with '/'.
     */
    @Column(name = "menu_url", length = 300)
    private String menuUrl;

    /** HTML link target. Default "_self". */
    @Builder.Default
    @Column(length = 20)
    private String target = "_self";

    // ── Hierarchy ─────────────────────────────────────────────────────────────

    /**
     * Parent menu ID (flat adjacency list).
     * null  → top-level MODULE.
     * Non-null → GROUP or LEAF child.
     *
     * Plain Long — NOT @ManyToOne — so the entire tree is one SELECT.
     */
    @Column(name = "parent_id")
    private Long parentId;

    /**
     * Hierarchy level — drives rendering and validation rules.
     *   MODULE → top tab (icon, no URL, has children)
     *   GROUP  → flyout section header (no URL, has LEAF children)
     *   LEAF   → clickable link (URL, no children)
     */
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "menu_type", length = 20, nullable = false)
    private MenuType menuType = MenuType.LEAF;

    /** Sort order within the same parent. Lower = first. */
    @Builder.Default
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    /**
     * Application module grouping (replaces old Module enum).
     * e.g. "PRODUCTION", "INVENTORY", "COMMERCIAL"
     * Optional — used only for grouping in the admin UI.
     */
    @Column(name = "module_name", length = 80)
    private String moduleName;

    // ── Permission (fallback filter) ──────────────────────────────────────────

    /**
     * Spring Security authority required to show this item when RoleMenuAccess
     * is NOT configured for the user's role (permission-fallback path).
     *
     * null/blank → visible to ALL authenticated users.
     * SUPER_ADMIN bypasses this field entirely.
     */
    @Column(name = "required_permission", length = 120)
    private String requiredPermission;

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    /**
     * Explicit show/hide flag (from old Menu.isVisible).
     * Use to temporarily hide an item without deactivating it.
     */
    @Builder.Default
    @Column(name = "visible", nullable = false)
    private boolean visible = true;

    /** Admin on/off toggle. Inactive items are hidden from all users. */
    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    /** Soft-delete — never physically remove rows. */
    @Builder.Default
    @Column(nullable = false)
    private boolean deleted = false;

    // ── Audit ─────────────────────────────────────────────────────────────────

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() { createdAt = updatedAt = LocalDateTime.now(); }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    // ── Enum ──────────────────────────────────────────────────────────────────

    public enum MenuType {
        /** Top-level tab — icon required, no URL, has GROUP/LEAF children. */
        MODULE,
        /** Non-clickable flyout header — no URL, has LEAF children. */
        GROUP,
        /** Clickable navigation link — URL required, no children. */
        LEAF
    }
}
