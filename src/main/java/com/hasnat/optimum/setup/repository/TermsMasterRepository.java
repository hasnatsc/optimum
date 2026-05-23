package com.hasnat.optimum.setup.repository;
import com.hasnat.optimum.common.enums.DocumentType;
import com.hasnat.optimum.setup.entity.TermsMaster;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public interface TermsMasterRepository extends JpaRepository<TermsMaster, Long> {
    List<TermsMaster> findByDocumentTypeAndIsActiveTrueOrderBySortOrder(DocumentType type);
    Optional<TermsMaster> findByDocumentTypeAndIsDefaultTrue(DocumentType type);
}
