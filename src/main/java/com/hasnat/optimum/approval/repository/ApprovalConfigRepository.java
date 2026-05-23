package com.hasnat.optimum.approval.repository;
import com.hasnat.optimum.approval.entity.ApprovalConfig;
import com.hasnat.optimum.common.enums.*;
import com.hasnat.optimum.common.enums.Module;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public interface ApprovalConfigRepository extends JpaRepository<ApprovalConfig, Long> {
    Optional<ApprovalConfig> findByCode(String code);
    List<ApprovalConfig> findByOrganizationIdAndIsActiveTrueOrderByName(Long orgId);
    List<ApprovalConfig> findByOrganizationIdAndDocumentType(Long orgId, ApprovalDocumentType type);
    List<ApprovalConfig> findByOrganizationIdAndModule(Long orgId, Module module);
    Optional<ApprovalConfig> findByOrganizationIdAndDocumentTypeAndCode(Long orgId,
        ApprovalDocumentType type, String code);
    boolean existsByOrganizationIdAndDocumentTypeAndCode(Long orgId,
        ApprovalDocumentType type, String code);
}
