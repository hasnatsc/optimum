package com.hasnat.optimum.organization.repository;
import com.hasnat.optimum.organization.entity.Department;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Optional<Department> findByCode(String code);
    Optional<Department> findByName(String name);
    List<Department> findByOrganizationIdAndActiveTrueOrderByName(Long orgId);
    List<Department> findByOrganizationId(Long orgId);
    List<Department> findByParentDepartmentIdIsNullAndOrganizationId(Long orgId);
    List<Department> findByParentDepartmentId(Long parentId);
    boolean existsByOrganizationIdAndCode(Long orgId, String code);
}
