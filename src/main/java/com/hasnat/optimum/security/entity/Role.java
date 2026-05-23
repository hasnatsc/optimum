package com.hasnat.optimum.security.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity @Table(name = "sec_roles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Role {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 250) private String nameBn;
    @Column(length = 255) private String description;

    @Enumerated(EnumType.STRING) @Column(length = 50)
    private MasterRole masterRole;

    @Column(nullable = false) private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "sec_role_permissions",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id"))
    @Builder.Default
    private Set<Permission> permissions = new LinkedHashSet<>();

    public enum MasterRole {
        ROLE_SUPER_ADMIN, ROLE_HRM, ROLE_ECOMMERCE_ADMIN, ROLE_ACCOUNTS_ADMIN,
        ROLE_SALES_MANAGER, ROLE_SALES_EXECUTIVE, ROLE_PURCHASE_MANAGER,
        ROLE_PURCHASE_OFFICER, ROLE_INVENTORY_MANAGER, ROLE_WAREHOUSE_STAFF,
        ROLE_ACCOUNTANT, ROLE_CUSTOMER_SUPPORT, ROLE_SUPPLIER,
        ROLE_PRODUCTION_MANAGER, ROLE_PRODUCTION_SUPERVISOR, ROLE_QUALITY_INSPECTOR,
        ROLE_COMMERCIAL_MANAGER, ROLE_EXPORT_OFFICER, ROLE_IMPORT_OFFICER,
        ROLE_COMMERCIAL_EXECUTIVE, ROLE_BOND_OFFICER, ROLE_DOCUMENTATION_OFFICER
    }
}
