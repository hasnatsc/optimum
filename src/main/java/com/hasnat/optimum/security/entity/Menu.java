package com.hasnat.optimum.security.entity;

import com.hasnat.optimum.common.enums.Module;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity @Table(name = "sec_menus")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Menu implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String menuCode;

    @Column(nullable = false, length = 100) private String menuName;
    @Column(length = 255)                   private String menuUrl;
    @Column(length = 255)                   private String description;
    @Column(length = 50)                    private String icon;
    @Column(length = 100)                   private String requiredPermission;
    @Column(length = 20)                    private String target;
    @Column(nullable = false)               private int displayOrder;
    @Column(nullable = false)               private boolean isActive;
    @Column(nullable = false)               private boolean isVisible;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING) @Column(length = 50)
    private Module module;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_menu_id")
    private Menu parentMenu;
}
