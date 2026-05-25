package com.hasnat.optimum.security.dto;

import com.hasnat.optimum.security.entity.AppMenu;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO for the merged AppMenu entity.
 * Carries all fields from both old Menu and old AppMenu.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AppMenuDTO {

    private Long   id;

    // From merged Menu
    private String menuCode;
    private String description;
    private String moduleName;
    private boolean visible;

    // From AppMenu (kept as-is)
    private String menuName;
    private String icon;
    private String menuUrl;
    private String target;
    private Long   parentId;
    private String parentName;
    private AppMenu.MenuType menuType;
    private Integer displayOrder;
    private String requiredPermission;
    private boolean active;

    // Audit
    private String        createdBy;
    private String        updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
