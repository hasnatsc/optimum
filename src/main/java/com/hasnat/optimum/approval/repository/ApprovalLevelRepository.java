package com.hasnat.optimum.approval.repository;
import com.hasnat.optimum.approval.entity.ApprovalLevel;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public interface ApprovalLevelRepository extends JpaRepository<ApprovalLevel, Long> {
    List<ApprovalLevel> findByApprovalConfigIdAndIsActiveTrueOrderByLevelNumber(Long configId);
    Optional<ApprovalLevel> findByApprovalConfigIdAndLevelNumber(Long configId, int levelNumber);
    List<ApprovalLevel> findByApproverUserId(Long userId);
    void deleteByApprovalConfigId(Long configId);
}
