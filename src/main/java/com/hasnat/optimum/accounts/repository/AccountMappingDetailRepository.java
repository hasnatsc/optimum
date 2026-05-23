package com.hasnat.optimum.accounts.repository;
import com.hasnat.optimum.accounts.entity.AccountMappingDetail;
import com.hasnat.optimum.accounts.entity.AccountMappingDetail.EntryType;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public interface AccountMappingDetailRepository extends JpaRepository<AccountMappingDetail, Long> {
    List<AccountMappingDetail> findByAccountsMappingIdAndIsActiveTrueOrderBySortOrder(Long mappingId);
    List<AccountMappingDetail> findByAccountsMappingIdAndEntryType(Long mappingId, EntryType type);
    void deleteByAccountsMappingId(Long mappingId);
}
