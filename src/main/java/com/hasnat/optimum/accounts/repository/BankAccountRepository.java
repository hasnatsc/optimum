package com.hasnat.optimum.accounts.repository;
import com.hasnat.optimum.accounts.entity.BankAccount;
import com.hasnat.optimum.accounts.entity.BankAccount.BankAccountType;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    Optional<BankAccount> findByAccountNumber(String accountNumber);
    Optional<BankAccount> findByBankAccountCode(String code);
    List<BankAccount> findByOrganizationIdAndIsActiveTrue(Long orgId);
    List<BankAccount> findByOrganizationIdAndBankAccountType(Long orgId, BankAccountType type);
    List<BankAccount> findByBankId(Long bankId);
    boolean existsByAccountNumber(String accountNumber);
    boolean existsByBankAccountCode(String code);
}
