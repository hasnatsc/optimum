package com.hasnat.optimum.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Runs immediately after a failed login attempt.
 *
 * Responsibilities:
 *  1. Translate each Spring Security exception into a specific URL query param
 *     so the login page can show an appropriate message.
 *  2. Track brute-force attempts in-memory (per IP).
 *     — For distributed deployments, move this to Redis or the DB.
 *  3. Log every failure for audit purposes.
 *
 * URL params appended to /login:
 *   ?error=bad_credentials  — wrong username or password
 *   ?error=locked           — account is locked
 *   ?error=disabled         — account is disabled
 *   ?error=expired          — credentials have expired
 *   ?error=account_expired  — account itself has expired
 *   ?error=too_many         — brute-force limit reached
 *   ?error=unknown          — any other exception
 */
@Slf4j
@Component
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    // ── Brute-force tracking (in-memory) ─────────────────────────────────────
    private static final int    MAX_ATTEMPTS     = 5;
    private static final long   WINDOW_MILLIS    = 15 * 60 * 1000L;  // 15 minutes

    /** IP → (attemptCount, windowStart) */
    private final ConcurrentHashMap<String, AttemptRecord> attempts = new ConcurrentHashMap<>();

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest   request,
            HttpServletResponse  response,
            AuthenticationException exception) throws IOException {

        String ip       = getClientIp(request);
        String username = request.getParameter("principal");

        // ── Brute-force check ─────────────────────────────────────────────────
        if (isBruteForce(ip)) {
            log.warn("[LOGIN BLOCKED] Too many attempts from ip='{}' user='{}'", ip, username);
            getRedirectStrategy().sendRedirect(request, response, "/login?error=too_many");
            return;
        }

        recordFailure(ip);

        // ── Map exception → URL param ─────────────────────────────────────────
        String errorParam = switch (exception) {
            case BadCredentialsException    e -> "bad_credentials";
            case UsernameNotFoundException  e -> "bad_credentials"; // same message (security)
            case LockedException            e -> "locked";
            case DisabledException          e -> "disabled";
            case CredentialsExpiredException e -> "expired";
            case AccountExpiredException    e -> "account_expired";
            default                           -> "unknown";
        };

        log.warn("[LOGIN FAILED] user='{}' ip='{}' reason='{}'",
            username, ip, exception.getClass().getSimpleName());

        getRedirectStrategy().sendRedirect(request, response,
            "/login?error=" + errorParam);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Brute-force helpers
    // ─────────────────────────────────────────────────────────────────────────

    private boolean isBruteForce(String ip) {
        AttemptRecord record = attempts.get(ip);
        if (record == null) return false;

        // Reset window if expired
        if (Instant.now().toEpochMilli() - record.windowStart > WINDOW_MILLIS) {
            attempts.remove(ip);
            return false;
        }

        return record.count >= MAX_ATTEMPTS;
    }

    private void recordFailure(String ip) {
        long now = Instant.now().toEpochMilli();
        attempts.compute(ip, (k, existing) -> {
            if (existing == null || now - existing.windowStart > WINDOW_MILLIS) {
                return new AttemptRecord(1, now);
            }
            return new AttemptRecord(existing.count + 1, existing.windowStart);
        });
    }

    /**
     * Call this from the success handler to reset the IP's attempt counter
     * after a successful login.
     */
    public void resetAttempts(String ip) {
        attempts.remove(ip);
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        return realIp != null ? realIp : request.getRemoteAddr();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Attempt record (immutable value object)
    // ─────────────────────────────────────────────────────────────────────────

    private record AttemptRecord(int count, long windowStart) {}
}
