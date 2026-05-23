package com.hasnat.optimum.setup.repository;
import com.hasnat.optimum.setup.entity.Bank;
import com.hasnat.optimum.setup.entity.Bank.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public interface BankRepository extends JpaRepository<Bank, Long>, JpaSpecificationExecutor<Bank> {
    Optional<Bank> findByOrganizationIdAndBankCode(Long orgId, String bankCode);
    Optional<Bank> findBySwiftCode(String swiftCode);
    List<Bank> findByOrganizationIdAndIsActiveTrue(Long orgId);
    List<Bank> findByOrganizationIdAndBankType(Long orgId, BankType bankType);
    List<Bank> findByOrganizationIdAndSupportsLcTrue(Long orgId);
    List<Bank> findByOrganizationIdAndSupportsImportLcTrue(Long orgId);
    List<Bank> findByOrganizationIdAndSupportsExportLcTrue(Long orgId);
    List<Bank> findByOrganizationIdAndSupportsBtbLcTrue(Long orgId);
    @Query("SELECT b FROM Bank b WHERE b.organization.id=:orgId AND b.isActive=true " +
           "AND (:type IS NULL OR b.bankType=:type) ORDER BY b.bankName")
    List<Bank> search(@Param("orgId") Long orgId, @Param("type") BankType type);
    boolean existsByOrganizationIdAndBankCode(Long orgId, String bankCode);
}
