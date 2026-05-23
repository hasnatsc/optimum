package com.hasnat.optimum.hrm.repository;
import com.hasnat.optimum.hrm.entity.Employee;
import com.hasnat.optimum.hrm.entity.Employee.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {
    Optional<Employee> findByEmployeeCode(String code);
    Optional<Employee> findByNationalId(String nationalId);
    Optional<Employee> findByPassportNumber(String passportNumber);
    Optional<Employee> findByPhone(String phone);
    Optional<Employee> findByEmail(String email);
    Optional<Employee> findByUserId(Long userId);
    List<Employee> findByOrganizationIdAndStatus(Long orgId, EmployeeStatus status);
    List<Employee> findByOrganizationIdAndDepartmentId(Long orgId, Long deptId);
    List<Employee> findByOrganizationIdAndDesignationId(Long orgId, Long designationId);
    List<Employee> findByReportingManagerId(Long managerId);
    long countByOrganizationIdAndStatus(Long orgId, EmployeeStatus status);
    @Query("SELECT e FROM Employee e WHERE e.organization.id=:orgId " +
           "AND e.status='ACTIVE' " +
           "AND (LOWER(e.firstName) LIKE LOWER(CONCAT('%',:q,'%')) " +
           "OR LOWER(e.lastName) LIKE LOWER(CONCAT('%',:q,'%')) " +
           "OR LOWER(e.employeeCode) LIKE LOWER(CONCAT('%',:q,'%')))")
    Page<Employee> search(@Param("orgId") Long orgId, @Param("q") String q, Pageable pageable);
    boolean existsByEmployeeCode(String code);
    boolean existsByOrganizationIdAndNationalId(Long orgId, String nationalId);
}
