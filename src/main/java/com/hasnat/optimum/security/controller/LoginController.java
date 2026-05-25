package com.hasnat.optimum.security.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Renders the three public authentication pages:
 *   GET /login               → templates/auth/login.html
 *   GET /forgot-password     → templates/auth/forgot-password.html
 *   GET /reset-password      → templates/auth/reset-password.html
 *
 * Spring Security processes POST /login and POST /logout itself.
 * The AJAX endpoints for forgot/reset are in AuthController.
 */
@Slf4j
@Controller
public class LoginController {

    // ─────────────────────────────────────────────────────────────────────────
    // GET /login
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * @param error    Spring Security appends this on failed authentication.
     *                 Value matches the error param set by CustomAuthenticationFailureHandler.
     *                 e.g. bad_credentials, locked, disabled, expired, too_many
     * @param logout   Spring Security appends this after /logout redirect.
     * @param invalid  Session management appends this for invalid/expired sessions.
     */
    @GetMapping("/login")
    public String loginPage(
            @RequestParam(required = false)           String  error,
            @RequestParam(required = false)           String  logout,
            @RequestParam(required = false)           String  invalid,
            Model model) {

        // ── Error message mapping ─────────────────────────────────────────────
        if (error != null) {
            String msg = switch (error) {
                case "bad_credentials"  -> "Invalid username or password. Please try again.";
                case "locked"           -> "Your account is locked. Contact your administrator.";
                case "disabled"         -> "Your account has been disabled.";
                case "expired"          -> "Your password has expired. Please reset it.";
                case "account_expired"  -> "Your account has expired. Contact your administrator.";
                case "too_many"         -> "Too many failed attempts. Please wait 15 minutes.";
                default                 -> "Authentication failed. Please try again.";
            };
            model.addAttribute("errorMessage", msg);
            model.addAttribute("alertType", "error");
        }

        // ── Logout confirmation ───────────────────────────────────────────────
        if (logout != null) {
            model.addAttribute("errorMessage", "You have been signed out successfully.");
            model.addAttribute("alertType", "success");
        }

        // ── Invalid / expired session ─────────────────────────────────────────
        if (invalid != null) {
            model.addAttribute("errorMessage", "Your session has expired. Please sign in again.");
            model.addAttribute("alertType", "warning");
        }

        return "auth/login";    // → src/main/resources/templates/auth/login.html
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /forgot-password
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "auth/forgot-password";  // → templates/auth/forgot-password.html
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /reset-password
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Token validation is done by the JS on page load via
     * GET /auth/validate-reset-token?token=…
     * This controller just renders the shell page.
     * If token is missing from the URL, the JS shows the "link expired" state.
     */
    @GetMapping("/reset-password")
    public String resetPasswordPage(
            @RequestParam(required = false) String token,
            Model model) {

        // Pass token to template — JS also reads it from the URL directly,
        // but having it in the model lets Thymeleaf pre-fill the hidden field.
        model.addAttribute("resetToken", token != null ? token : "");
        return "auth/reset-password";  // → templates/auth/reset-password.html
    }
}
