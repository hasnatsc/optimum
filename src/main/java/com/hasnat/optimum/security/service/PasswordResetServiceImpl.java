package com.hasnat.optimum.security.service;

import com.hasnat.optimum.security.entity.PasswordResetToken;
import com.hasnat.optimum.security.entity.User;
import com.hasnat.optimum.security.repository.PasswordResetTokenRepository;
import com.hasnat.optimum.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Password-reset flow implementation.
 *
 * Email sending:
 *   spring-boot-starter-mail is NOT in the current pom.xml.
 *   In development the reset link is logged at WARN level.
 *   To enable real email:
 *     1. Add spring-boot-starter-mail to pom.xml
 *     2. Configure spring.mail.* in application.properties
 *     3. Inject JavaMailSender and replace the log.warn() block below.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserRepository               userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder              passwordEncoder;

    /** Token validity window (minutes). Configurable via application.properties. */
    @Value("${app.security.reset-token-ttl-minutes:60}")
    private int tokenTtlMinutes;

    /** Base URL used to build the reset link. Override in each environment. */
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    // ─────────────────────────────────────────────────────────────────────────
    // 1. Initiate reset
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public boolean initiateReset(String email) {

        Optional<User> userOpt = userRepository.findByEmail(email);

        // Never reveal whether the email exists — silently succeed regardless.
        if (userOpt.isEmpty()) {
            log.debug("[PASSWORD RESET] No user found for email='{}' — ignoring silently", email);
            return true;
        }

        User user = userOpt.get();

        // Invalidate any previous pending tokens for this user
        int invalidated = tokenRepository.invalidatePreviousTokens(user, LocalDateTime.now());
        if (invalidated > 0) {
            log.debug("[PASSWORD RESET] Invalidated {} previous token(s) for user='{}'",
                invalidated, user.getUsername());
        }

        // Create a new UUID token
        String rawToken = UUID.randomUUID().toString().replace("-", "");
        PasswordResetToken token = PasswordResetToken.builder()
            .token(rawToken)
            .user(user)
            .expiresAt(LocalDateTime.now().plusMinutes(tokenTtlMinutes))
            .build();

        tokenRepository.save(token);

        String resetLink = baseUrl + "/reset-password?token=" + rawToken;

        // ── TODO: Replace this block with JavaMailSender when mail is configured ──
        //
        // Example with JavaMailSender:
        //   SimpleMailMessage msg = new SimpleMailMessage();
        //   msg.setTo(user.getEmail());
        //   msg.setSubject("Spindles ERP — Password Reset");
        //   msg.setText("Click here to reset your password: " + resetLink +
        //               "\n\nThis link expires in " + tokenTtlMinutes + " minutes.");
        //   mailSender.send(msg);
        //
        // ── DEV MODE: log the link instead ──────────────────────────────────────
        log.warn("""
            ============================================================
            [PASSWORD RESET] Dev mode — email not sent.
            User    : {}
            Email   : {}
            Link    : {}
            Expires : {} minutes
            ============================================================
            """,
            user.getUsername(), user.getEmail(), resetLink, tokenTtlMinutes
        );

        return true;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. Validate token
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public boolean isTokenValid(String token) {
        if (token == null || token.isBlank()) return false;
        return tokenRepository
            .findValidToken(token, LocalDateTime.now())
            .isPresent();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. Apply the new password
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void resetPassword(String tokenValue, String newPassword) {

        PasswordResetToken token = tokenRepository
            .findValidToken(tokenValue, LocalDateTime.now())
            .orElseThrow(() -> new IllegalArgumentException(
                "Password reset token is invalid or has expired."));

        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));

        // If the account was flagged as expired credentials, clear that flag
        user.setCredentialsNonExpired(true);

        userRepository.save(user);

        // Mark token as used (cannot be redeemed again)
        token.markUsed();
        tokenRepository.save(token);

        log.info("[PASSWORD RESET] Password updated for user='{}' id={}",
            user.getUsername(), user.getId());
    }
}
