package com.hasnat.optimum.security.repository;

import com.hasnat.optimum.security.entity.AppMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for the merged AppMenu entity (replaces both old MenuRepository
 * and the previous AppMenuRepository that referenced the old package).
 *
 * Package: com.hasnat.optimum.security.repository
 * Entity:  com.hasnat.optimum.security.entity.AppMenu
 * Table:   app_menus
 */
@Repository
public interface AppMenuRepository extends JpaRepository<AppMenu, Long> {

    // ── User-menu query (hot path — called on every page load) ────────────────

    /**
     * All active, visible, non-deleted menus in display order.
     * One SELECT, no joins. Permission filtering happens in Java.
     */
    @Query("""
        SELECT m FROM AppMenu m
        WHERE m.active  = true
          AND m.deleted = false
        ORDER BY m.displayOrder ASC, m.id ASC
    """)
    List<AppMenu> findAllActiveOrdered();

    // ── Admin CRUD ────────────────────────────────────────────────────────────

    /** All non-deleted menus (incl. inactive) for the admin DataTable. */
    @Query("""
        SELECT m FROM AppMenu m
        WHERE m.deleted = false
        ORDER BY m.displayOrder ASC, m.id ASC
    """)
    List<AppMenu> findAllNotDeleted();

    /**
     * Active parent candidates for the parent-picker dropdown.
     * Only MODULE and GROUP can be parents.
     */
    @Query("""
        SELECT m FROM AppMenu m
        WHERE m.active   = true
          AND m.deleted  = false
          AND m.menuType IN (
              com.hasnat.optimum.security.entity.AppMenu.MenuType.MODULE,
              com.hasnat.optimum.security.entity.AppMenu.MenuType.GROUP
          )
        ORDER BY m.displayOrder ASC, m.menuName ASC
    """)
    List<AppMenu> findActiveParentCandidates();

    /** Direct children — used when cascading soft-deletes. */
    List<AppMenu> findByParentIdAndDeletedFalse(Long parentId);

    // ── Uniqueness checks ─────────────────────────────────────────────────────

    Optional<AppMenu> findByMenuCode(String menuCode);

    boolean existsByMenuCode(String menuCode);

    boolean existsByMenuCodeAndIdNot(String menuCode, Long id);

    boolean existsByMenuNameAndParentIdAndDeletedFalse(String menuName, Long parentId);

    boolean existsByMenuNameAndParentIdAndIdNotAndDeletedFalse(
            String menuName, Long parentId, Long id);

    // ── Display order ─────────────────────────────────────────────────────────

    /**
     * Max displayOrder for siblings — used to append a new item at the end.
     * The :parentId param is null for top-level items.
     */
    @Query("""
        SELECT COALESCE(MAX(m.displayOrder), 0)
        FROM AppMenu m
        WHERE m.deleted = false
          AND ((:parentId IS NULL AND m.parentId IS NULL)
               OR m.parentId = :parentId)
    """)
    int findMaxDisplayOrderByParentId(@Param("parentId") Long parentId);

    // ── Single-record lookups ─────────────────────────────────────────────────

    Optional<AppMenu> findByIdAndDeletedFalse(Long id);

    // ── Select2 AJAX search ───────────────────────────────────────────────────

    @Query("""
        SELECT m FROM AppMenu m
        WHERE m.active  = true
          AND m.deleted = false
          AND (:search IS NULL OR :search = ''
               OR LOWER(m.menuName) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(m.menuUrl) LIKE LOWER(CONCAT('%', :search, '%')))
        ORDER BY m.displayOrder ASC, m.menuName ASC
    """)
    List<AppMenu> searchMenus(@Param("search") String search);
}
