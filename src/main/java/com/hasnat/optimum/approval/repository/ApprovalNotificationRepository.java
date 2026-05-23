package com.hasnat.optimum.approval.repository;
import com.hasnat.optimum.approval.entity.ApprovalNotification;
import com.hasnat.optimum.approval.entity.ApprovalNotification.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public interface ApprovalNotificationRepository extends JpaRepository<ApprovalNotification, Long> {
    List<ApprovalNotification> findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);
    List<ApprovalNotification> findByApprovalRequestId(Long requestId);
    Page<ApprovalNotification> findByRecipientIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    long countByRecipientIdAndIsReadFalse(Long userId);
    @Modifying
    @Query("UPDATE ApprovalNotification n SET n.isRead=true, n.readAt=CURRENT_TIMESTAMP " +
           "WHERE n.recipient.id=:userId AND n.isRead=false")
    int markAllReadForUser(@Param("userId") Long userId);
    @Modifying
    @Query("UPDATE ApprovalNotification n SET n.isRead=true, n.readAt=CURRENT_TIMESTAMP WHERE n.id=:id")
    int markRead(@Param("id") Long id);
    List<ApprovalNotification> findByDeliveryStatus(DeliveryStatus status);
}
