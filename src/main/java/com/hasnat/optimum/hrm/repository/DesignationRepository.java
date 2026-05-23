package com.hasnat.optimum.hrm.repository;
import com.hasnat.optimum.hrm.entity.Designation;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public interface DesignationRepository extends JpaRepository<Designation, Long> {
    Optional<Designation> findByOrganizationIdAndDesignationCode(Long orgId, String code);
    List<Designation> findByOrganizationIdAndIsActiveTrueOrderByDesignationName(Long orgId);
    List<Designation> findByOrganizationIdAndGrade(Long orgId, String grade);
    boolean existsByOrganizationIdAndDesignationCode(Long orgId, String code);
}
