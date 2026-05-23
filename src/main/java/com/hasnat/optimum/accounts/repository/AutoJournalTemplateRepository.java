package com.hasnat.optimum.accounts.repository;
import com.hasnat.optimum.accounts.entity.AutoJournalTemplate;
import com.hasnat.optimum.accounts.entity.AutoJournalTemplate.*;
import com.hasnat.optimum.common.enums.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public interface AutoJournalTemplateRepository extends JpaRepository<AutoJournalTemplate, Long> {
    Optional<AutoJournalTemplate> findByOrganizationIdAndTemplateCode(Long orgId, String code);
    List<AutoJournalTemplate> findByOrganizationIdAndIsActiveTrueOrderByTemplateName(Long orgId);
    List<AutoJournalTemplate> findByOrganizationIdAndModuleType(Long orgId, ModuleType moduleType);
    List<AutoJournalTemplate> findByOrganizationIdAndTriggerMode(Long orgId, TriggerMode mode);
    List<AutoJournalTemplate> findByOrganizationIdAndTransactionType(Long orgId, TransactionType txnType);
    boolean existsByOrganizationIdAndTemplateCode(Long orgId, String code);
}
