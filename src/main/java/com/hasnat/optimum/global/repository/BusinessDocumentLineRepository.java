package com.hasnat.optimum.global.repository;
import com.hasnat.optimum.global.entity.BusinessDocumentLine;
import com.hasnat.optimum.global.entity.BusinessDocumentLine.QualityStatus;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public interface BusinessDocumentLineRepository extends JpaRepository<BusinessDocumentLine, Long> {
    List<BusinessDocumentLine> findByDocumentIdOrderByLineNumber(Long documentId);
    List<BusinessDocumentLine> findByItemIdAndDocumentOrganizationId(Long itemId, Long orgId);
    List<BusinessDocumentLine> findByDocumentIdAndQualityStatus(Long documentId, QualityStatus status);
    Optional<BusinessDocumentLine> findByDocumentIdAndLineNumber(Long documentId, int lineNumber);
    void deleteByDocumentId(Long documentId);
}
