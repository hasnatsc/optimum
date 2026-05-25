package com.hasnat.optimum.security.dto;

import com.hasnat.optimum.security.entity.AppMenu;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppMenuDTO {

    private Long   id;
    private String menuName;
    private String icon;
    private String menuUrl;
    private String target;

    /** Parent menu id — null for top-level MODULE items. */
    private Long   parentId;

    /** Human-readable parent name — populated when reading from DB for display. */
    private String parentName;

    private AppMenu.MenuType menuType;

    private Integer displayOrder;

    /**
     * Spring Security authority required to see this menu item.
     * e.g.  "PERM_PRODUCTION_ORDER_VIEW"
     * Null / blank = visible to all authenticated users.
     */
    private String requiredPermission;

    private boolean active;

    // ── Audit ────────────────────────────────────────────────────────────
    private String        createdBy;
    private String        updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
