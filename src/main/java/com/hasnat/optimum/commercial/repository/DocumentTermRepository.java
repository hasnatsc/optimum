package com.hasnat.optimum.commercial.repository;
import com.hasnat.optimum.commercial.entity.DocumentTerm;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public interface DocumentTermRepository extends JpaRepository<DocumentTerm, Long> {
    List<DocumentTerm> findByDocumentIdOrderBySortOrder(Long documentId);
    List<DocumentTerm> findByInvoiceIdOrderBySortOrder(Long invoiceId);
    void deleteByDocumentId(Long documentId);
    void deleteByInvoiceId(Long invoiceId);
}
