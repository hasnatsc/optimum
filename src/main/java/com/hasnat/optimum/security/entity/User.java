package com.hasnat.optimum.security.entity;

import com.hasnat.optimum.organization.entity.Organization;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity @Table(name = "sec_users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, unique = true, length = 20)
    private String phone;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(length = 100) private String fullName;

    @Column(nullable = false) private boolean enabled;
    @Column(nullable = false) private boolean accountNonExpired;
    @Column(nullable = false) private boolean accountNonLocked;
    @Column(nullable = false) private boolean credentialsNonExpired;

    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING) @Column(length = 35)
    private DefaultDashboard defaultDashboard;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "sec_user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id"))
    @Builder.Default
    private Set<Role> roles = new LinkedHashSet<>();

    public enum DefaultDashboard {
        CORE_SECURITY, ACCESS_MENU, HRM, SALES_CUSTOMER_OPERATIONS, PURCHASE_SUPPLIER,
        INVENTORY_WAREHOUSE, FINANCE_ACCOUNTS, PRODUCTION, PRODUCT_CATALOG_ECOMMERCE,
        POS, CRM, COMMUNICATION_NOTIFICATION, COMMERCIAL, REPORTS_ANALYTICS
    }
}
