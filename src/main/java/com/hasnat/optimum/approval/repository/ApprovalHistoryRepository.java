package com.hasnat.optimum.approval.repository;
import com.hasnat.optimum.approval.entity.ApprovalHistory;
import com.hasnat.optimum.approval.entity.ApprovalHistory.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public interface ApprovalHistoryRepository extends JpaRepository<ApprovalHistory, Long> {
    List<ApprovalHistory> findByApprovalRequestIdOrderByActionAtDesc(Long requestId);
    List<ApprovalHistory> findByActorUserIdOrderByActionAtDesc(Long userId);
    List<ApprovalHistory> findByApprovalRequestIdAndAction(Long requestId, HistoryAction action);
    Optional<ApprovalHistory> findFirstByApprovalRequestIdOrderByActionAtDesc(Long requestId);
}
