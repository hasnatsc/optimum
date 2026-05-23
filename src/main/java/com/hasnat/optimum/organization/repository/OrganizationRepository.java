package com.hasnat.optimum.organization.repository;
import com.hasnat.optimum.organization.entity.Organization;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    Optional<Organization> findByCode(String code);
    List<Organization> findByIsActiveTrueOrderByName();
    boolean existsByCode(String code);
}
