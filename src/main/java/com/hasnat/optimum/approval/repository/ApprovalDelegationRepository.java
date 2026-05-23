package com.hasnat.optimum.approval.repository;
import com.hasnat.optimum.approval.entity.ApprovalDelegation;
import com.hasnat.optimum.approval.entity.ApprovalDelegation.DelegationStatus;
import com.hasnat.optimum.common.enums.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.*;
@Repository
public interface ApprovalDelegationRepository extends JpaRepository<ApprovalDelegation, Long> {
    Optional<ApprovalDelegation> findByDelegationCode(String code);
    List<ApprovalDelegation> findByDelegatorIdAndStatus(Long delegatorId, DelegationStatus status);
    List<ApprovalDelegation> findByDelegateIdAndStatus(Long delegateId, DelegationStatus status);
    @Query("SELECT d FROM ApprovalDelegation d WHERE d.delegate.id=:userId " +
           "AND d.status='ACTIVE' AND d.startDate <= :today AND d.endDate >= :today")
    List<ApprovalDelegation> findActiveForDelegate(@Param("userId") Long userId,
                                                    @Param("today") LocalDate today);
    @Query("SELECT d FROM ApprovalDelegation d WHERE d.status='SCHEDULED' AND d.startDate <= :today")
    List<ApprovalDelegation> findDueToActivate(@Param("today") LocalDate today);
    @Query("SELECT d FROM ApprovalDelegation d WHERE d.status='ACTIVE' AND d.endDate < :today")
    List<ApprovalDelegation> findExpired(@Param("today") LocalDate today);
}
