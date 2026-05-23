package com.hasnat.optimum.accounts.repository;
import com.hasnat.optimum.accounts.entity.AccountMapping;
import com.hasnat.optimum.common.enums.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public interface AccountMappingRepository extends JpaRepository<AccountMapping, Long> {
    Optional<AccountMapping> findByOrganizationIdAndMappingCode(Long orgId, String code);
    List<AccountMapping> findByOrganizationIdAndIsActiveTrueOrderByMappingName(Long orgId);
    List<AccountMapping> findByOrganizationIdAndModuleType(Long orgId, ModuleType moduleType);
    List<AccountMapping> findByOrganizationIdAndTransactionType(Long orgId, TransactionType txnType);
    Optional<AccountMapping> findByOrganizationIdAndModuleTypeAndTransactionTypeAndIsDefaultTrue(
        Long orgId, ModuleType moduleType, TransactionType txnType);
    boolean existsByOrganizationIdAndMappingCode(Long orgId, String code);
}
