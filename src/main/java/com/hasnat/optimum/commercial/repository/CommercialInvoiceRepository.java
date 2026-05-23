package com.hasnat.optimum.commercial.repository;
import com.hasnat.optimum.commercial.entity.CommercialInvoice;
import com.hasnat.optimum.commercial.entity.CommercialInvoice.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.*;
@Repository
public interface CommercialInvoiceRepository extends JpaRepository<CommercialInvoice, Long>,
        JpaSpecificationExecutor<CommercialInvoice> {
    Optional<CommercialInvoice> findByInvoiceNo(String invoiceNo);
    List<CommercialInvoice> findByOrganizationIdAndInvoiceType(Long orgId, InvoiceType type);
    List<CommercialInvoice> findByOrganizationIdAndStatus(Long orgId, InvoiceStatus status);
    List<CommercialInvoice> findByLcId(Long lcId);
    List<CommercialInvoice> findByPartyId(Long partyId);
    @Query("SELECT ci FROM CommercialInvoice ci WHERE ci.organization.id=:orgId " +
           "AND ci.invoiceDate BETWEEN :from AND :to ORDER BY ci.invoiceDate DESC")
    List<CommercialInvoice> findByDateRange(@Param("orgId") Long orgId,
        @Param("from") LocalDate from, @Param("to") LocalDate to);
    @Query("SELECT ci FROM CommercialInvoice ci WHERE ci.organization.id=:orgId " +
           "AND (LOWER(ci.invoiceNo) LIKE LOWER(CONCAT('%',:q,'%')) " +
           "OR LOWER(ci.blNumber) LIKE LOWER(CONCAT('%',:q,'%')))")
    Page<CommercialInvoice> search(@Param("orgId") Long orgId, @Param("q") String q, Pageable pageable);
    boolean existsByInvoiceNo(String invoiceNo);
}
