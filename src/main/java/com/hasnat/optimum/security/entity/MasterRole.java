package com.hasnat.optimum.security.entity;

/**
 * Top-level system roles.
 * Each Role entity has exactly one MasterRole that maps to a Spring Security
 * ROLE_* authority string.
 *
 * Permission checks via @PreAuthorize use fine-grained PERM_* names,
 * not these coarse-grained roles — so add new modules without changing this enum.
 */
public enum MasterRole {

    ROLE_SUPER_ADMIN,   // full system access — bypasses all @PreAuthorize checks
    ROLE_ADMIN,         // cross-module admin, cannot manage other admins
    ROLE_PRODUCTION,    // production floor users
    ROLE_INVENTORY,     // warehouse / inventory users
    ROLE_COMMERCIAL,    // sales & export users
    ROLE_HR,            // HR users
    ROLE_FINANCE,       // finance users
    ROLE_VIEWER         // read-only across all permitted modules
}
