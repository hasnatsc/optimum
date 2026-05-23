package com.hasnat.optimum.accounts.repository;
import com.hasnat.optimum.accounts.entity.CashAccount;
import com.hasnat.optimum.accounts.entity.CashAccount.CashAccountType;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public interface CashAccountRepository extends JpaRepository<CashAccount, Long> {
    Optional<CashAccount> findByCashAccountCode(String code);
    List<CashAccount> findByOrganizationIdAndIsActiveTrue(Long orgId);
    List<CashAccount> findByOrganizationIdAndCashAccountType(Long orgId, CashAccountType type);
    boolean existsByCashAccountCode(String code);
}
