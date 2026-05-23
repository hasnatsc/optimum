package com.hasnat.optimum.security.repository;
import com.hasnat.optimum.common.enums.Module;
import com.hasnat.optimum.security.entity.Permission;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByName(String name);
    Optional<Permission> findByUrlPatternAndHttpMethod(String urlPattern, String httpMethod);
    List<Permission> findByModule(Module module);
    List<Permission> findByActiveTrueOrderByModule();
    boolean existsByUrlPatternAndHttpMethod(String urlPattern, String httpMethod);
}
