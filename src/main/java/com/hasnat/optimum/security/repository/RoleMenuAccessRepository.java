package com.hasnat.optimum.security.repository;
import com.hasnat.optimum.security.entity.RoleMenuAccess;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public interface RoleMenuAccessRepository extends JpaRepository<RoleMenuAccess, Long> {
    List<RoleMenuAccess> findByRoleId(Long roleId);
    List<RoleMenuAccess> findByMenuId(Long menuId);
    Optional<RoleMenuAccess> findByRoleIdAndMenuId(Long roleId, Long menuId);
    void deleteByRoleId(Long roleId);
    boolean existsByRoleIdAndMenuId(Long roleId, Long menuId);
}
