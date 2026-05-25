package com.hasnat.optimum.security.repository;

import com.hasnat.optimum.security.entity.AppMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppMenuRepository extends JpaRepository<AppMenu, Long> {

    // ── User-menu query (performance-critical) ────────────────────────────

    /**
     * Load every active, non-deleted menu in display order.
     * One query — no joins.  Permission filtering is done in Java
     * to avoid a complex SQL predicate that varies per user.
     */
    @Query("""
        SELECT m FROM AppMenu m
        WHERE m.active  = true
          AND m.deleted = false
        ORDER BY m.displayOrder ASC, m.id ASC
    """)
    List<AppMenu> findAllActiveOrdered();

    // ── Admin CRUD ────────────────────────────────────────────────────────

    /** All non-deleted menus (including inactive) for the admin DataTable. */
    @Query("""
        SELECT m FROM AppMenu m
        WHERE m.deleted = false
        ORDER BY m.displayOrder ASC, m.id ASC
    """)
    List<AppMenu> findAllNotDeleted();

    /**
     * Active non-deleted menus for the parent-selector dropdown.
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

    /** Direct children of a given parent — used when deleting a parent. */
    List<AppMenu> findByParentIdAndDeletedFalse(Long parentId);

    /** Check if a menu name already exists at the same level. */
    boolean existsByMenuNameAndParentIdAndDeletedFalse(String menuName, Long parentId);

    boolean existsByMenuNameAndParentIdAndIdNotAndDeletedFalse(
            String menuName, Long parentId, Long id);

    /** Max displayOrder among siblings — used to append a new item at the end. */
    @Query("""
        SELECT COALESCE(MAX(m.displayOrder), 0)
        FROM AppMenu m
        WHERE m.deleted  = false
          AND ((:parentId IS NULL AND m.parentId IS NULL)
               OR m.parentId = :parentId)
    """)
    int findMaxDisplayOrderByParentId(@Param("parentId") Long parentId);

    /** Find non-deleted by id. */
    Optional<AppMenu> findByIdAndDeletedFalse(Long id);

    /**
     * Search for Select2 AJAX — used when picking a menu item in other forms.
     */
    @Query("""
        SELECT m FROM AppMenu m
        WHERE m.active  = true
          AND m.deleted = false
          AND (:search IS NULL OR :search = ''
               OR LOWER(m.menuName) LIKE LOWER(CONCAT('%', :search, '%')))
        ORDER BY m.displayOrder ASC, m.menuName ASC
    """)
    List<AppMenu> searchMenus(@Param("search") String search);
}
