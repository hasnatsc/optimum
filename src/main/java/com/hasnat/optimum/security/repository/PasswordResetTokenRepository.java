package com.hasnat.optimum.security.repository;
import com.hasnat.optimum.security.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.*;
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    Optional<PasswordResetToken> findByUserId(Long userId);
    void deleteByUserId(Long userId);
    void deleteByExpiryDateBefore(LocalDateTime dateTime);
}
