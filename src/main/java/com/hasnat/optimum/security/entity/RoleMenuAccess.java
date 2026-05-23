package com.hasnat.optimum.security.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "sec_mrole_menus")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RoleMenuAccess {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @Column(nullable = false) private boolean canView;
    @Column(nullable = false) private boolean canCreate;
    @Column(nullable = false) private boolean canEdit;
    @Column(nullable = false) private boolean canDelete;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
