package com.hasnat.optimum.security.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Navigation menu item.
 *
 * Tree structure is stored as an adjacency list (parentId column).
 * The JS buildMenuTree() function reconstructs the hierarchy client-side
 * from the flat list returned by /openApi/menus/user-menu.
 *
 * Three-level hierarchy:
 *   MODULE  → top-level tab (icon, no URL)
 *     GROUP   → non-clickable flyout header (no URL)
 *       LEAF  → clickable link (URL, requiredPermission)
 *
 * Table: app_menus
 */
@Entity
@Table(
    name = "app_menus",
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
public class AppMenu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Display ───────────────────────────────────────────────────────────

    @Column(nullable = false, length = 120)
    private String menuName;

    /**
     * Font-Awesome class string for MODULE-level icons.
     * e.g. "fa fa-industry"  "fa fa-boxes-stacked"
     * Null / blank for GROUP and LEAF items.
     */
    @Column(length = 100)
    private String icon;

    /**
     * URL path for LEAF items — must start with '/'.
     * Null for MODULE and GROUP.
     */
    @Column(length = 300)
    private String menuUrl;

    /**
     * Link target attribute.  Default "_self".
     */
    @Builder.Default
    @Column(length = 20)
    private String target = "_self";

    // ── Hierarchy ─────────────────────────────────────────────────────────

    /**
     * Parent menu id.
     * NULL  → top-level MODULE item.
     * Non-null → child of that parent (GROUP or LEAF).
     *
     * Stored as a plain Long (not @ManyToOne) to avoid N+1 queries
     * when loading the full menu tree in one query.
     */
    @Column(name = "parent_id")
    private Long parentId;

    /**
     * Menu type — used for validation and admin UI rendering.
     * The JS renderer infers type from position in the tree, so this
     * field is optional for the client but useful for the backend.
     */
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "menu_type", length = 20, nullable = false)
    private MenuType menuType = MenuType.LEAF;

    /** Sort order within the same parent level. Lower = first. */
    @Builder.Default
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    // ── Permission ────────────────────────────────────────────────────────

    /**
     * Spring Security authority required to see this item.
     * e.g. "PERM_PRODUCTION_ORDER_VIEW"
     *
     * NULL or blank → visible to ALL authenticated users.
     * SUPER_ADMIN always sees every item regardless of this field.
     */
    @Column(name = "required_permission", length = 120)
    private String requiredPermission;

    // ── Lifecycle ─────────────────────────────────────────────────────────

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    @Builder.Default
    @Column(nullable = false)
    private boolean deleted = false;

    // ── Audit ─────────────────────────────────────────────────────────────

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

    // ── Enum ──────────────────────────────────────────────────────────────

    public enum MenuType {
        /** Top-level tab — has icon, no URL, has GROUP/LEAF children. */
        MODULE,
        /** Non-clickable section header — no URL, has LEAF children. */
        GROUP,
        /** Clickable navigation link — has URL, no children. */
        LEAF
    }
}
