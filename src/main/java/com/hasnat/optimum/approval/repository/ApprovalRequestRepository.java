package com.hasnat.optimum.approval.repository;
import com.hasnat.optimum.approval.entity.ApprovalRequest;
import com.hasnat.optimum.approval.entity.ApprovalRequest.RequestStatus;
import com.hasnat.optimum.common.enums.ApprovalDocumentType;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.*;
@Repository
public interface ApprovalRequestRepository extends JpaRepository<ApprovalRequest, Long>,
        JpaSpecificationExecutor<ApprovalRequest> {
    Optional<ApprovalRequest> findByDocumentTypeAndReferenceId(ApprovalDocumentType type, Long refId);
    List<ApprovalRequest> findByOrganizationIdAndStatus(Long orgId, RequestStatus status);
    List<ApprovalRequest> findByRequesterIdAndStatus(Long requesterId, RequestStatus status);
    List<ApprovalRequest> findByCurrentApproverUserIdAndStatus(Long userId, RequestStatus status);
    List<ApprovalRequest> findByOrganizationIdAndDocumentType(Long orgId, ApprovalDocumentType type);
    List<ApprovalRequest> findByOrganizationIdAndIsUrgentTrueAndStatus(Long orgId, RequestStatus status);
    @Query("SELECT r FROM ApprovalRequest r WHERE r.organization.id=:orgId " +
           "AND r.currentApproverUser.id=:userId " +
           "AND r.status IN ('IN_APPROVAL','SUBMITTED') ORDER BY r.isUrgent DESC, r.dueDate ASC")
    List<ApprovalRequest> findPendingForApprover(@Param("orgId") Long orgId,
                                                  @Param("userId") Long userId);
    @Query("SELECT r FROM ApprovalRequest r WHERE r.organization.id=:orgId " +
           "AND r.dueDate < :today AND r.status IN ('IN_APPROVAL','SUBMITTED')")
    List<ApprovalRequest> findOverdue(@Param("orgId") Long orgId, @Param("today") LocalDate today);
    long countByOrganizationIdAndStatus(Long orgId, RequestStatus status);
    long countByCurrentApproverUserIdAndStatus(Long userId, RequestStatus status);
    Page<ApprovalRequest> findByRequesterIdOrderByCreatedAtDesc(Long requesterId, Pageable pageable);
}
