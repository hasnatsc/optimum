package com.hasnat.optimum.accounts.repository;
import com.hasnat.optimum.accounts.entity.AutoJournalTemplateLine;
import com.hasnat.optimum.accounts.entity.AccountMappingDetail.EntryType;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public interface AutoJournalTemplateLineRepository extends JpaRepository<AutoJournalTemplateLine, Long> {
    List<AutoJournalTemplateLine> findByAutoJournalTemplateIdAndIsActiveTrueOrderBySortOrder(Long templateId);
    List<AutoJournalTemplateLine> findByAutoJournalTemplateIdAndEntryType(Long templateId, EntryType type);
    void deleteByAutoJournalTemplateId(Long templateId);
}
