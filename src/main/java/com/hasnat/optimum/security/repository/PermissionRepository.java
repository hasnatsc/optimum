// ─────────────────────────────────────────────────────────────────────────────
// FILE: PermissionRepository.java
// ─────────────────────────────────────────────────────────────────────────────
package com.hasnat.optimum.security.repository;

import com.hasnat.optimum.security.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByName(String name);

    boolean existsByName(String name);

    List<Permission> findByModuleOrderByName(String module);

    /** All permissions grouped by module — for the role assignment UI. */
    @Query("SELECT p FROM Permission p ORDER BY p.module, p.name")
    List<Permission> findAllOrderedByModule();

    /** Search for Select2 dropdown. */
    @Query("""
        SELECT p FROM Permission p
        WHERE (:search IS NULL OR :search = ''
               OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%')))
        ORDER BY p.module, p.name
    """)
    List<Permission> searchPermissions(@Param("search") String search);
}
