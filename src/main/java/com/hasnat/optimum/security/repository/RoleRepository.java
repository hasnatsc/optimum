package com.hasnat.optimum.security.repository;

import com.hasnat.optimum.security.entity.MasterRole;
import com.hasnat.optimum.security.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByMasterRole(MasterRole masterRole);

    boolean existsByMasterRole(MasterRole masterRole);

    List<Role> findByActiveTrueOrderByName();

    /**
     * Select2 AJAX search — returns active roles matching the search term.
     * Returns: id (as String for Select2), text (display)
     */
    @Query("""
        SELECT r FROM Role r
        WHERE r.active = true
          AND (:search IS NULL OR :search = ''
               OR LOWER(r.name) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(r.description) LIKE LOWER(CONCAT('%', :search, '%')))
        ORDER BY r.name
    """)
    List<Role> searchRoles(@Param("search") String search);
}
