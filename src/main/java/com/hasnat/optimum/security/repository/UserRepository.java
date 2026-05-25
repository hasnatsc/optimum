package com.hasnat.optimum.security.repository;

import com.hasnat.optimum.security.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // ── Login lookup (used by CustomUserDetailsService) ────────────────────────

    /**
     * Multi-field login: accepts username OR email OR phone as the principal.
     * Only active, non-deleted users are returned.
     */
    @Query("""
        SELECT u FROM User u
        WHERE u.deleted = false
          AND (u.username = :username
               OR u.email = :email
               OR u.phone = :phone)
    """)
    Optional<User> findByUsernameOrEmailOrPhone(
        @Param("username") String username,
        @Param("email")    String email,
        @Param("phone")    String phone
    );

    // ── Password reset (used by PasswordResetServiceImpl) ─────────────────────

    @Query("SELECT u FROM User u WHERE u.email = :email AND u.deleted = false")
    Optional<User> findByEmail(@Param("email") String email);

    // ── Existence checks (used by UserServiceImpl for validation) ──────────────

    boolean existsByUsernameAndDeletedFalse(String username);

    boolean existsByEmailAndDeletedFalse(String email);

    boolean existsByUsernameAndIdNotAndDeletedFalse(String username, Long id);

    boolean existsByEmailAndIdNotAndDeletedFalse(String email, Long id);

    // ── Single user lookup (non-deleted only) ──────────────────────────────────

    @Query("SELECT u FROM User u WHERE u.id = :id AND u.deleted = false")
    Optional<User> findActiveById(@Param("id") Long id);

    // ── Audit: record last login timestamp ────────────────────────────────────

    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :ts WHERE u.id = :id")
    void updateLastLogin(@Param("id") Long id, @Param("ts") LocalDateTime ts);
}
