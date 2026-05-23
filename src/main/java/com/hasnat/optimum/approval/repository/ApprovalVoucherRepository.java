package com.hasnat.optimum.approval.repository;
import com.hasnat.optimum.approval.entity.ApprovalVoucher;
import com.hasnat.optimum.approval.entity.ApprovalVoucher.ApprovalStatus;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public interface ApprovalVoucherRepository extends JpaRepository<ApprovalVoucher, Long> {
    Optional<ApprovalVoucher> findByJournalEntryMasterId(Long journalEntryMasterId);
    List<ApprovalVoucher> findByApprovalStatus(ApprovalStatus status);
    List<ApprovalVoucher> findByApproverName(String approverName);
}
