package com.hasnat.optimum.security.repository;
import com.hasnat.optimum.common.enums.Module;
import com.hasnat.optimum.security.entity.Menu;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {
    Optional<Menu> findByMenuCode(String menuCode);
    List<Menu> findByParentMenuIdIsNullAndIsActiveTrueOrderByDisplayOrder();
    List<Menu> findByParentMenuIdAndIsActiveTrueOrderByDisplayOrder(Long parentId);
    List<Menu> findByModuleAndIsActiveTrueOrderByDisplayOrder(Module module);
    @Query("SELECT m FROM Menu m WHERE m.isActive=true AND m.isVisible=true " +
           "AND m.parentMenu IS NULL ORDER BY m.displayOrder")
    List<Menu> findTopLevelVisible();
    @Query("SELECT m FROM Menu m JOIN RoleMenuAccess rma ON rma.menu=m " +
           "JOIN rma.role r JOIN r.permissions p WHERE r.id IN :roleIds " +
           "AND m.isActive=true AND m.isVisible=true ORDER BY m.displayOrder")
    List<Menu> findAccessibleByRoles(@Param("roleIds") List<Long> roleIds);
}
