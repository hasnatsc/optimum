package com.hasnat.optimum.accounts.repository;
import com.hasnat.optimum.accounts.entity.JournalEntryMaster;
import com.hasnat.optimum.common.enums.VoucherType;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.*;
@Repository
public interface JournalEntryMasterRepository extends JpaRepository<JournalEntryMaster, Long> {
    Optional<JournalEntryMaster> findByVoucherNo(String voucherNo);
    List<JournalEntryMaster> findByOrganizationIdAndVoucherType(Long orgId, VoucherType type);
    List<JournalEntryMaster> findByOrganizationIdAndVoucherDateBetween(Long orgId,
        LocalDate from, LocalDate to);
    List<JournalEntryMaster> findByOrganizationIdAndIsPostedTrue(Long orgId);
}
