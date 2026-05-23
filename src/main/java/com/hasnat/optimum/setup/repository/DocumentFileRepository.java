package com.hasnat.optimum.setup.repository;
import com.hasnat.optimum.common.enums.DocumentType;
import com.hasnat.optimum.setup.entity.DocumentFile;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public interface DocumentFileRepository extends JpaRepository<DocumentFile, Long> {
    List<DocumentFile> findByDocumentTypeAndReferenceId(DocumentType type, Long referenceId);
    long countByDocumentTypeAndReferenceId(DocumentType type, Long referenceId);
    void deleteByDocumentTypeAndReferenceId(DocumentType type, Long referenceId);
}
