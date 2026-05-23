package com.hasnat.optimum.global.repository;
import com.hasnat.optimum.common.enums.DocumentType;
import com.hasnat.optimum.global.entity.BusinessDocument;
import com.hasnat.optimum.global.entity.BusinessDocument.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.*;
@Repository
public interface BusinessDocumentRepository extends JpaRepository<BusinessDocument, Long>,
        JpaSpecificationExecutor<BusinessDocument> {
    Optional<BusinessDocument> findByDocumentNo(String documentNo);
    List<BusinessDocument> findByOrganizationIdAndDocumentType(Long orgId, DocumentType type);
    List<BusinessDocument> findByOrganizationIdAndStatus(Long orgId, DocumentStatus status);
    List<BusinessDocument> findByOrganizationIdAndDocumentTypeAndStatus(
        Long orgId, DocumentType type, DocumentStatus status);
    List<BusinessDocument> findByParentDocumentId(Long parentId);
    List<BusinessDocument> findByPartyId(Long partyId);
    List<BusinessDocument> findByWarehouseIdAndDocumentType(Long warehouseId, DocumentType type);
    @Query("SELECT d FROM BusinessDocument d WHERE d.organization.id=:orgId " +
           "AND d.documentType=:type AND d.isDeleted=false " +
           "AND d.documentDate BETWEEN :from AND :to ORDER BY d.documentDate DESC")
    List<BusinessDocument> findByDateRange(@Param("orgId") Long orgId,
        @Param("type") DocumentType type, @Param("from") LocalDate from, @Param("to") LocalDate to);
    @Query("SELECT d FROM BusinessDocument d WHERE d.organization.id=:orgId " +
           "AND d.isDeleted=false " +
           "AND (LOWER(d.documentNo) LIKE LOWER(CONCAT('%',:q,'%')) " +
           "OR LOWER(d.referenceNo) LIKE LOWER(CONCAT('%',:q,'%')))")
    Page<BusinessDocument> search(@Param("orgId") Long orgId, @Param("q") String q, Pageable pageable);
    @Query("SELECT d FROM BusinessDocument d WHERE d.organization.id=:orgId " +
           "AND d.documentType=:type AND d.status NOT IN ('CANCELLED','COMPLETED','CONVERTED') " +
           "AND d.isDeleted=false AND d.party IS NOT NULL")
    List<BusinessDocument> findOpen(@Param("orgId") Long orgId, @Param("type") DocumentType type);
    @Modifying
    @Query("UPDATE BusinessDocument d SET d.isDeleted=true, d.deletedAt=CURRENT_TIMESTAMP, " +
           "d.deletedBy=:deletedBy WHERE d.id=:id")
    int softDelete(@Param("id") Long id, @Param("deletedBy") String deletedBy);
    long countByOrganizationIdAndDocumentTypeAndStatus(Long orgId, DocumentType type, DocumentStatus status);
    boolean existsByDocumentNo(String documentNo);
}
