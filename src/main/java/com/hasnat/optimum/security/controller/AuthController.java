package com.hasnat.optimum.security.controller;

import com.hasnat.optimum.security.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST endpoints consumed by the login-system JavaScript pages.
 * All paths are declared public in SecurityConfig (.permitAll()).
 *
 * POST /auth/forgot-password        — initiate password reset
 * GET  /auth/validate-reset-token   — check if a token is still valid
 * POST /auth/reset-password         — apply new password
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final PasswordResetService passwordResetService;

    // ─────────────────────────────────────────────────────────────────────────
    // POST /auth/forgot-password
    // Body: { "email": "user@example.com" }
    // ─────────────────────────────────────────────────────────────────────────

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(
            @RequestBody Map<String, String> body) {

        String email = body.get("email");

        // Basic input guard
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Email address is required."
            ));
        }

        try {
            // Service never reveals whether the email exists (security best practice).
            passwordResetService.initiateReset(email.trim().toLowerCase());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "If an account exists for this email, a reset link has been sent."
            ));

        } catch (Exception e) {
            log.error("[FORGOT PASSWORD] Unexpected error for email='{}': {}", email, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "An unexpected error occurred. Please try again."
            ));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /auth/validate-reset-token?token=abc123
    // Called on page load by reset-password.html to detect expired links early.
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/validate-reset-token")
    public ResponseEntity<Map<String, Object>> validateToken(
            @RequestParam String token) {

        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success",      false,
                "tokenInvalid", true,
                "message",      "Token is required."
            ));
        }

        boolean valid = passwordResetService.isTokenValid(token);

        if (valid) {
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Token is valid."
            ));
        } else {
            return ResponseEntity.ok(Map.of(
                "success",      false,
                "tokenExpired", true,
                "message",      "This reset link has expired or has already been used."
            ));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /auth/reset-password
    // Body: { "token": "abc123", "password": "NewPass1!" }
    // ─────────────────────────────────────────────────────────────────────────

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(
            @RequestBody Map<String, String> body) {

        String token       = body.get("token");
        String newPassword = body.get("password");

        // ── Input validation ──────────────────────────────────────────────────
        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success",      false,
                "tokenInvalid", true,
                "message",      "Reset token is missing."
            ));
        }

        if (newPassword == null || newPassword.length() < 8) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Password must be at least 8 characters."
            ));
        }

        // ── Apply the reset ───────────────────────────────────────────────────
        try {
            passwordResetService.resetPassword(token, newPassword);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Your password has been updated successfully."
            ));

        } catch (IllegalArgumentException e) {
            // Token invalid / expired / already used
            log.warn("[RESET PASSWORD] Token rejected: {}", e.getMessage());
            return ResponseEntity.ok(Map.of(
                "success",      false,
                "tokenExpired", true,
                "message",      e.getMessage()
            ));

        } catch (Exception e) {
            log.error("[RESET PASSWORD] Unexpected error: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "An unexpected error occurred. Please try again."
            ));
        }
    }
}
