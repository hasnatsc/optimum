package com.hasnat.optimum.commercial.repository;
import com.hasnat.optimum.commercial.entity.LetterOfCredit;
import com.hasnat.optimum.commercial.entity.LetterOfCredit.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.*;
@Repository
public interface LetterOfCreditRepository extends JpaRepository<LetterOfCredit, Long>,
        JpaSpecificationExecutor<LetterOfCredit> {
    Optional<LetterOfCredit> findByLcNumber(String lcNumber);
    Optional<LetterOfCredit> findByManualLcNumber(String manualLcNumber);
    List<LetterOfCredit> findByOrganizationIdAndStatus(Long orgId, LcStatus status);
    List<LetterOfCredit> findByOrganizationIdAndLcType(Long orgId, LcType type);
    List<LetterOfCredit> findByCustomerId(Long customerId);
    List<LetterOfCredit> findBySupplierId(Long supplierId);
    @Query("SELECT lc FROM LetterOfCredit lc WHERE lc.organization.id=:orgId " +
           "AND lc.status NOT IN ('CLOSED','CANCELLED') " +
           "AND lc.expiryDate BETWEEN :from AND :to ORDER BY lc.expiryDate ASC")
    List<LetterOfCredit> findExpiringBetween(@Param("orgId") Long orgId,
        @Param("from") LocalDate from, @Param("to") LocalDate to);
    @Query("SELECT lc FROM LetterOfCredit lc WHERE lc.organization.id=:orgId " +
           "AND lc.status NOT IN ('CLOSED','CANCELLED') " +
           "AND (LOWER(lc.lcNumber) LIKE LOWER(CONCAT('%',:q,'%')) " +
           "OR LOWER(lc.manualLcNumber) LIKE LOWER(CONCAT('%',:q,'%')))")
    Page<LetterOfCredit> search(@Param("orgId") Long orgId, @Param("q") String q, Pageable pageable);
    boolean existsByLcNumber(String lcNumber);
}
