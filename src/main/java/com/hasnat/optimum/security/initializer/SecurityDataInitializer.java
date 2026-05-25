package com.hasnat.optimum.security.initializer;

import com.hasnat.optimum.security.entity.*;
import com.hasnat.optimum.security.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Idempotent security data seeder.
 *
 * Run order: @Order(1) — runs before any other ApplicationRunner.
 *
 * On every startup it:
 *   1. Ensures all PERM_* permission records exist (creates missing ones).
 *   2. Ensures all roles exist with their default permission sets.
 *   3. Ensures a default superadmin user exists.
 *
 * Safe to run repeatedly — skips records that already exist.
 *
 * Default credentials:
 *   username : superadmin
 *   password : Admin@1234   ← CHANGE IMMEDIATELY in production
 */
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class SecurityDataInitializer implements ApplicationRunner {

    private final PermissionRepository permissionRepository;
    private final RoleRepository       roleRepository;
    private final UserRepository       userRepository;
    private final PasswordEncoder      passwordEncoder;

    // ── Permission catalogue ──────────────────────────────────────────────────
    // Format: "PERM_NAME::description::MODULE"

    private static final List<String[]> PERMISSION_CATALOGUE = List.of(

        // USER MANAGEMENT
        new String[]{"PERM_USER_VIEW",              "View users",                   "USER_MANAGEMENT"},
        new String[]{"PERM_USER_CREATE",            "Create new users",             "USER_MANAGEMENT"},
        new String[]{"PERM_USER_EDIT",              "Edit user details",            "USER_MANAGEMENT"},
        new String[]{"PERM_USER_DELETE",            "Delete (soft) users",          "USER_MANAGEMENT"},
        new String[]{"PERM_USER_CHANGE_PASSWORD",   "Reset any user password",      "USER_MANAGEMENT"},
        new String[]{"PERM_USER_TOGGLE_STATUS",     "Enable / disable users",       "USER_MANAGEMENT"},

        // ROLE MANAGEMENT
        new String[]{"PERM_ROLE_VIEW",              "View roles",                   "ROLE_MANAGEMENT"},
        new String[]{"PERM_ROLE_CREATE",            "Create roles",                 "ROLE_MANAGEMENT"},
        new String[]{"PERM_ROLE_EDIT",              "Edit roles & permissions",     "ROLE_MANAGEMENT"},
        new String[]{"PERM_ROLE_DELETE",            "Delete roles",                 "ROLE_MANAGEMENT"},

        // ORGANIZATION / SETUP
        new String[]{"PERM_ORG_VIEW",               "View organizations",           "ORGANIZATION"},
        new String[]{"PERM_ORG_CREATE",             "Create organizations",         "ORGANIZATION"},
        new String[]{"PERM_ORG_EDIT",               "Edit organizations",           "ORGANIZATION"},
        new String[]{"PERM_ORG_DELETE",             "Delete organizations",         "ORGANIZATION"},
        new String[]{"PERM_BUSINESS_UNIT_VIEW",     "View business units",          "ORGANIZATION"},
        new String[]{"PERM_BUSINESS_UNIT_MANAGE",   "Manage business units",        "ORGANIZATION"},
        new String[]{"PERM_COST_CENTER_VIEW",       "View cost centers",            "ORGANIZATION"},
        new String[]{"PERM_COST_CENTER_MANAGE",     "Manage cost centers",          "ORGANIZATION"},
        new String[]{"PERM_WAREHOUSE_VIEW",         "View warehouses",              "ORGANIZATION"},
        new String[]{"PERM_WAREHOUSE_MANAGE",       "Manage warehouses",            "ORGANIZATION"},

        // PRODUCTION
        new String[]{"PERM_PRODUCTION_ORDER_VIEW",      "View production orders",   "PRODUCTION"},
        new String[]{"PERM_PRODUCTION_ORDER_CREATE",    "Create production orders", "PRODUCTION"},
        new String[]{"PERM_PRODUCTION_ORDER_EDIT",      "Edit production orders",   "PRODUCTION"},
        new String[]{"PERM_PRODUCTION_ORDER_DELETE",    "Delete production orders", "PRODUCTION"},
        new String[]{"PERM_PRODUCTION_ORDER_SUBMIT",    "Submit for approval",      "PRODUCTION"},
        new String[]{"PERM_PRODUCTION_ORDER_APPROVE",   "Approve production orders","PRODUCTION"},
        new String[]{"PERM_PRODUCTION_ORDER_REJECT",    "Reject production orders", "PRODUCTION"},
        new String[]{"PERM_PRODUCTION_ORDER_RELEASE",   "Release to production",    "PRODUCTION"},
        new String[]{"PERM_PRODUCTION_ORDER_START",     "Start production",         "PRODUCTION"},
        new String[]{"PERM_PRODUCTION_ORDER_COMPLETE",  "Complete production",      "PRODUCTION"},
        new String[]{"PERM_PRODUCTION_ORDER_CANCEL",    "Cancel production orders", "PRODUCTION"},

        // INVENTORY
        new String[]{"PERM_INVENTORY_ITEM_VIEW",    "View inventory items",         "INVENTORY"},
        new String[]{"PERM_INVENTORY_ITEM_CREATE",  "Create inventory items",       "INVENTORY"},
        new String[]{"PERM_INVENTORY_ITEM_EDIT",    "Edit inventory items",         "INVENTORY"},
        new String[]{"PERM_INVENTORY_ITEM_DELETE",  "Delete inventory items",       "INVENTORY"},
        new String[]{"PERM_INVENTORY_RECEIPT_VIEW", "View stock receipts",          "INVENTORY"},
        new String[]{"PERM_INVENTORY_RECEIPT_MANAGE","Manage stock receipts",       "INVENTORY"},
        new String[]{"PERM_INVENTORY_ISSUE_VIEW",   "View stock issues",            "INVENTORY"},
        new String[]{"PERM_INVENTORY_ISSUE_MANAGE", "Manage stock issues",          "INVENTORY"},

        // COMMERCIAL
        new String[]{"PERM_COMMERCIAL_PI_VIEW",     "View proforma invoices",       "COMMERCIAL"},
        new String[]{"PERM_COMMERCIAL_PI_CREATE",   "Create proforma invoices",     "COMMERCIAL"},
        new String[]{"PERM_COMMERCIAL_PI_EDIT",     "Edit proforma invoices",       "COMMERCIAL"},
        new String[]{"PERM_COMMERCIAL_PI_DELETE",   "Delete proforma invoices",     "COMMERCIAL"},
        new String[]{"PERM_COMMERCIAL_SO_VIEW",     "View sales orders",            "COMMERCIAL"},
        new String[]{"PERM_COMMERCIAL_SO_MANAGE",   "Manage sales orders",          "COMMERCIAL"},
        new String[]{"PERM_COMMERCIAL_LC_VIEW",     "View letters of credit",       "COMMERCIAL"},
        new String[]{"PERM_COMMERCIAL_LC_MANAGE",   "Manage letters of credit",     "COMMERCIAL"},

        // REPORTS
        new String[]{"PERM_REPORT_VIEW",            "View reports",                 "REPORTS"},
        new String[]{"PERM_REPORT_EXPORT",          "Export reports",               "REPORTS"},

        // SYSTEM
        new String[]{"PERM_MENU_MANAGE",            "Manage navigation menus",      "SYSTEM"},
        new String[]{"PERM_AUDIT_LOG_VIEW",         "View audit logs",              "SYSTEM"},
        new String[]{"PERM_APPROVAL_INBOX_VIEW",    "View approval inbox",          "SYSTEM"},
        new String[]{"PERM_APPROVAL_ACTION",        "Perform approval actions",     "SYSTEM"}
    );

    // ─────────────────────────────────────────────────────────────────────────
    // Entry point
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("[INIT] Starting security data initialization…");

        Map<String, Permission> permissions = initPermissions();
        initRoles(permissions);
        initSuperAdmin();

        log.info("[INIT] Security data initialization complete.");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Step 1 — Permissions
    // ─────────────────────────────────────────────────────────────────────────

    private Map<String, Permission> initPermissions() {
        Map<String, Permission> permMap = new LinkedHashMap<>();
        int created = 0;

        for (String[] row : PERMISSION_CATALOGUE) {
            String name        = row[0];
            String description = row[1];
            String module      = row[2];

            Permission perm = permissionRepository.findByName(name).orElseGet(() -> {
                Permission p = Permission.builder()
                    .name(name)
                    .description(description)
                    .module(module)
                    .build();
                return permissionRepository.save(p);
            });

            permMap.put(name, perm);
            if (perm.getId() != null) created++;
        }

        log.info("[INIT] Permissions: {} total, {} newly created",
            permMap.size(), created - (permMap.size() - created));
        return permMap;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Step 2 — Roles
    // ─────────────────────────────────────────────────────────────────────────

    private void initRoles(Map<String, Permission> p) {
        createRoleIfAbsent(
            MasterRole.ROLE_SUPER_ADMIN,
            "Super Administrator",
            "Full unrestricted access to all modules and settings.",
            // SUPER_ADMIN gets every permission
            p.values().toArray(new Permission[0])
        );

        createRoleIfAbsent(
            MasterRole.ROLE_ADMIN,
            "Administrator",
            "Cross-module administrative access excluding security settings.",
            perms(p,
                "PERM_USER_VIEW",      "PERM_USER_CREATE",   "PERM_USER_EDIT",
                "PERM_USER_TOGGLE_STATUS", "PERM_USER_CHANGE_PASSWORD",
                "PERM_ROLE_VIEW",
                "PERM_ORG_VIEW",       "PERM_BUSINESS_UNIT_VIEW", "PERM_COST_CENTER_VIEW",
                "PERM_WAREHOUSE_VIEW", "PERM_WAREHOUSE_MANAGE",
                "PERM_PRODUCTION_ORDER_VIEW", "PERM_PRODUCTION_ORDER_CREATE",
                "PERM_PRODUCTION_ORDER_EDIT", "PERM_PRODUCTION_ORDER_APPROVE",
                "PERM_PRODUCTION_ORDER_REJECT", "PERM_PRODUCTION_ORDER_RELEASE",
                "PERM_INVENTORY_ITEM_VIEW", "PERM_INVENTORY_RECEIPT_VIEW",
                "PERM_COMMERCIAL_PI_VIEW",   "PERM_COMMERCIAL_SO_VIEW",
                "PERM_REPORT_VIEW",    "PERM_REPORT_EXPORT",
                "PERM_APPROVAL_INBOX_VIEW", "PERM_APPROVAL_ACTION"
            )
        );

        createRoleIfAbsent(
            MasterRole.ROLE_PRODUCTION,
            "Production User",
            "Creates and manages production orders through their lifecycle.",
            perms(p,
                "PERM_PRODUCTION_ORDER_VIEW",   "PERM_PRODUCTION_ORDER_CREATE",
                "PERM_PRODUCTION_ORDER_EDIT",   "PERM_PRODUCTION_ORDER_DELETE",
                "PERM_PRODUCTION_ORDER_SUBMIT", "PERM_PRODUCTION_ORDER_START",
                "PERM_PRODUCTION_ORDER_COMPLETE","PERM_PRODUCTION_ORDER_CANCEL",
                "PERM_INVENTORY_ITEM_VIEW",     "PERM_INVENTORY_RECEIPT_VIEW",
                "PERM_COMMERCIAL_PI_VIEW",      "PERM_COMMERCIAL_SO_VIEW",
                "PERM_REPORT_VIEW",             "PERM_APPROVAL_INBOX_VIEW"
            )
        );

        createRoleIfAbsent(
            MasterRole.ROLE_INVENTORY,
            "Inventory User",
            "Manages inventory items, receipts, and stock issues.",
            perms(p,
                "PERM_INVENTORY_ITEM_VIEW",     "PERM_INVENTORY_ITEM_CREATE",
                "PERM_INVENTORY_ITEM_EDIT",
                "PERM_INVENTORY_RECEIPT_VIEW",  "PERM_INVENTORY_RECEIPT_MANAGE",
                "PERM_INVENTORY_ISSUE_VIEW",    "PERM_INVENTORY_ISSUE_MANAGE",
                "PERM_PRODUCTION_ORDER_VIEW",   "PERM_WAREHOUSE_VIEW",
                "PERM_REPORT_VIEW"
            )
        );

        createRoleIfAbsent(
            MasterRole.ROLE_COMMERCIAL,
            "Commercial User",
            "Manages sales orders, proforma invoices, and letters of credit.",
            perms(p,
                "PERM_COMMERCIAL_PI_VIEW",   "PERM_COMMERCIAL_PI_CREATE",
                "PERM_COMMERCIAL_PI_EDIT",   "PERM_COMMERCIAL_PI_DELETE",
                "PERM_COMMERCIAL_SO_VIEW",   "PERM_COMMERCIAL_SO_MANAGE",
                "PERM_COMMERCIAL_LC_VIEW",   "PERM_COMMERCIAL_LC_MANAGE",
                "PERM_INVENTORY_ITEM_VIEW",
                "PERM_REPORT_VIEW",          "PERM_REPORT_EXPORT"
            )
        );

        createRoleIfAbsent(
            MasterRole.ROLE_VIEWER,
            "Viewer",
            "Read-only access across all permitted modules.",
            perms(p,
                "PERM_PRODUCTION_ORDER_VIEW",
                "PERM_INVENTORY_ITEM_VIEW",
                "PERM_COMMERCIAL_PI_VIEW",
                "PERM_REPORT_VIEW"
            )
        );

        log.info("[INIT] Roles initialised: {}", roleRepository.count());
    }

    private void createRoleIfAbsent(MasterRole masterRole, String name,
                                    String description, Permission... perms) {
        if (roleRepository.existsByMasterRole(masterRole)) {
            log.debug("[INIT] Role {} already exists — skipped", masterRole);
            return;
        }

        Role role = Role.builder()
            .masterRole(masterRole)
            .name(name)
            .description(description)
            .active(true)
            .permissions(new LinkedHashSet<>(Arrays.asList(perms)))
            .build();

        roleRepository.save(role);
        log.info("[INIT] Created role: {} with {} permissions", masterRole, perms.length);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Step 3 — Default superadmin user
    // ─────────────────────────────────────────────────────────────────────────

    private void initSuperAdmin() {
        if (userRepository.existsByUsernameAndDeletedFalse("superadmin")) {
            log.debug("[INIT] superadmin user already exists — skipped");
            return;
        }

        Role superAdminRole = roleRepository.findByMasterRole(MasterRole.ROLE_SUPER_ADMIN)
            .orElseThrow(() -> new IllegalStateException(
                "ROLE_SUPER_ADMIN not found — roles must be initialised first"));

        User admin = User.builder()
            .username("superadmin")
            .email("superadmin@optimum.local")
            .phone("+880000000000")
            .fullName("Super Administrator")
            // ⚠️  CHANGE THIS PASSWORD IMMEDIATELY in production!
            .password(passwordEncoder.encode("Admin@1234"))
            .enabled(true)
            .accountNonExpired(true)
            .accountNonLocked(true)
            .credentialsNonExpired(true)
            .defaultDashboard(User.DefaultDashboard.DEFAULT)
            .createdBy("SYSTEM")
            .updatedBy("SYSTEM")
            .roles(new LinkedHashSet<>(Set.of(superAdminRole)))
            .build();

        userRepository.save(admin);

        log.warn("""
            ╔══════════════════════════════════════════════════════╗
            ║          DEFAULT SUPERADMIN CREATED                  ║
            ║  username : superadmin                               ║
            ║  password : Admin@1234                               ║
            ║  ⚠  CHANGE THIS PASSWORD IMMEDIATELY IN PRODUCTION  ║
            ╚══════════════════════════════════════════════════════╝
            """);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Utility — resolve permission names to entities
    // ─────────────────────────────────────────────────────────────────────────

    private Permission[] perms(Map<String, Permission> map, String... names) {
        return Arrays.stream(names)
            .map(name -> {
                Permission p = map.get(name);
                if (p == null) log.warn("[INIT] Permission not found in catalogue: {}", name);
                return p;
            })
            .filter(Objects::nonNull)
            .toArray(Permission[]::new);
    }
}
