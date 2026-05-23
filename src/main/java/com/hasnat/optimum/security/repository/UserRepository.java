package com.hasnat.optimum.security.repository;
import com.hasnat.optimum.security.entity.User;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);
    Optional<User> findByUsernameOrEmail(String username, String email);
    List<User> findByOrganizationIdAndEnabledTrue(Long orgId);
    List<User> findByOrganizationId(Long orgId);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.id = :roleId AND u.organization.id = :orgId")
    List<User> findByOrganizationIdAndRoleId(@Param("orgId") Long orgId, @Param("roleId") Long roleId);
    @Query("SELECT u FROM User u WHERE u.organization.id = :orgId " +
           "AND (LOWER(u.fullName) LIKE LOWER(CONCAT('%',:q,'%')) " +
           "OR LOWER(u.username) LIKE LOWER(CONCAT('%',:q,'%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT('%',:q,'%')))")
    List<User> search(@Param("orgId") Long orgId, @Param("q") String q);


    /**
     * Single query — tries username, email, and phone in one shot.
     * Spring generates the JPQL automatically from the method name.
     */
    Optional<User> findByUsernameOrEmailOrPhone(
            String username, String email, String phone);

    @Query("""
           SELECT u FROM User u
           JOIN FETCH u.roles r
           JOIN FETCH r.permissions
           WHERE u.username = :username
           """)
    Optional<User> findWithRolesAndPermissions(String username);
}
