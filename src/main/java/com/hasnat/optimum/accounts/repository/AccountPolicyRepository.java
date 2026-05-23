package com.hasnat.optimum.accounts.repository;
import com.hasnat.optimum.accounts.entity.AccountPolicy;
import com.hasnat.optimum.common.enums.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public interface AccountPolicyRepository extends JpaRepository<AccountPolicy, Long> {
    Optional<AccountPolicy> findByOrganizationIdAndPolicyCode(Long orgId, String code);
    List<AccountPolicy> findByOrganizationIdAndIsActiveTrueOrderByPolicyName(Long orgId);
    List<AccountPolicy> findByOrganizationIdAndPolicyType(Long orgId, VoucherType policyType);
    Optional<AccountPolicy> findByOrganizationIdAndPolicyTypeAndIsDefaultTrue(Long orgId, VoucherType type);
    boolean existsByOrganizationIdAndPolicyCode(Long orgId, String code);
}
