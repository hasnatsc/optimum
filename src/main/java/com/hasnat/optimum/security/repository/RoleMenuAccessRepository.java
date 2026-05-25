package com.hasnat.optimum.security.repository;

import com.hasnat.optimum.security.entity.AppMenu;
import com.hasnat.optimum.security.entity.Role;
import com.hasnat.optimum.security.entity.RoleMenuAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Updated to reference AppMenu (merged entity) instead of the removed Menu.
 * canView = true is the gate for user-menu visibility.
 */
@Repository
public interface RoleMenuAccessRepository extends JpaRepository<RoleMenuAccess, Long> {

    // ── User-menu filtering (called on every page load) ───────────────────────

    /**
     * Returns menu IDs where canView = true for any of the given role IDs.
     * This is the hot path — one query, no extra joins needed.
     */
    @Query("""
        SELECT rma.menu.id
        FROM RoleMenuAccess rma
        WHERE rma.role.id IN :roleIds
          AND rma.canView = true
    """)
    Set<Long> findViewableMenuIdsByRoleIds(@Param("roleIds") Set<Long> roleIds);

    /**
     * True if ANY of the given roles has at least one canView = true entry.
     * Used to decide PATH A (role-based) vs PATH B (permission-fallback).
     */
    @Query("""
        SELECT COUNT(rma) > 0
        FROM RoleMenuAccess rma
        WHERE rma.role.id IN :roleIds
          AND rma.canView = true
    """)
    boolean existsViewableByRoleIds(@Param("roleIds") Set<Long> roleIds);

    // ── Admin CRUD ────────────────────────────────────────────────────────────

    /** All access entries for a role (for the assignment page). */
    List<RoleMenuAccess> findByRole(Role role);

    /** Specific entry for a role + menu pair (to check current flags). */
    Optional<RoleMenuAccess> findByRoleAndMenu(Role role, AppMenu menu);

    /** All menu IDs assigned to a role (any canView value). */
    @Query("SELECT rma.menu.id FROM RoleMenuAccess rma WHERE rma.role = :role")
    Set<Long> findMenuIdsByRole(@Param("role") Role role);

    /**
     * Delete all entries for a role before re-saving the full access set.
     * Called inside a transaction before saveAll().
     */
    @Modifying
    @Query("DELETE FROM RoleMenuAccess rma WHERE rma.role = :role")
    void deleteByRole(@Param("role") Role role);

    /** Count of canView=true menus for a role — shown in the Role DataTable. */
    @Query("""
        SELECT COUNT(rma)
        FROM RoleMenuAccess rma
        WHERE rma.role.id = :roleId
          AND rma.canView = true
    """)
    long countViewableByRoleId(@Param("roleId") Long roleId);
}
