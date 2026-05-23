package com.hasnat.optimum.commercial.repository;
import com.hasnat.optimum.commercial.entity.LcDocumentMapping;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.*;
@Repository
public interface LcDocumentMappingRepository extends JpaRepository<LcDocumentMapping, Long> {
    List<LcDocumentMapping> findByLcId(Long lcId);
    List<LcDocumentMapping> findByDocumentId(Long documentId);
    Optional<LcDocumentMapping> findByLcIdAndDocumentId(Long lcId, Long documentId);
    @Query("SELECT COALESCE(SUM(m.utilizedAmount), 0) FROM LcDocumentMapping m WHERE m.lc.id=:lcId")
    BigDecimal sumUtilizedByLc(@Param("lcId") Long lcId);
    @Query("SELECT COALESCE(SUM(m.allocatedAmount), 0) FROM LcDocumentMapping m WHERE m.lc.id=:lcId")
    BigDecimal sumAllocatedByLc(@Param("lcId") Long lcId);
}
