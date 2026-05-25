package com.hasnat.optimum.security.repository;

import com.hasnat.optimum.security.entity.PasswordResetToken;
import com.hasnat.optimum.security.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    /** Find a valid (unused, non-expired) token by its value. */
    @Query("""
        SELECT t FROM PasswordResetToken t
        WHERE t.token = :token
          AND t.usedAt IS NULL
          AND t.expiresAt > :now
    """)
    Optional<PasswordResetToken> findValidToken(
        @Param("token") String token,
        @Param("now")   LocalDateTime now
    );

    /** Find any token by value (regardless of state — for validation messages). */
    Optional<PasswordResetToken> findByToken(String token);

    /**
     * Invalidate all previous unused tokens for this user before issuing a new one.
     * Prevents token accumulation and closes the window where an old link still works.
     */
    @Modifying
    @Query("""
        UPDATE PasswordResetToken t
        SET t.usedAt = :now
        WHERE t.user = :user
          AND t.usedAt IS NULL
    """)
    int invalidatePreviousTokens(@Param("user") User user, @Param("now") LocalDateTime now);

    /**
     * Scheduled cleanup — delete tokens older than retentionDays.
     * Call from a @Scheduled method in a maintenance service.
     */
    @Modifying
    @Query("""
        DELETE FROM PasswordResetToken t
        WHERE t.createdAt < :cutoff
    """)
    int deleteExpiredBefore(@Param("cutoff") LocalDateTime cutoff);
}
