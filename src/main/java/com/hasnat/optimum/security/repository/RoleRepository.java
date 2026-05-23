package com.hasnat.optimum.security.repository;
import com.hasnat.optimum.security.entity.Role;
import com.hasnat.optimum.security.entity.Role.MasterRole;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
    Optional<Role> findByMasterRole(MasterRole masterRole);
    List<Role> findByActiveTrueOrderByName();
    boolean existsByName(String name);
}
