package com.hasnat.optimum.security.handler;

import com.hasnat.optimum.security.service.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Runs immediately after a successful login.
 *
 * Responsibilities:
 *  1. Clear any stale failure-count stored in the session.
 *  2. Store lightweight user info in the session for Thymeleaf fragments.
 *  3. Redirect to the user's default dashboard (role-based) or a
 *     previously-requested URL saved by Spring's RequestCache.
 */
@Slf4j
@Component
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    // Session attribute keys — must match what Thymeleaf / JS reads
    public static final String SESSION_USER_ID       = "userId";
    public static final String SESSION_USERNAME      = "username";
    public static final String SESSION_FULL_NAME     = "fullName";
    public static final String SESSION_EMAIL         = "email";
    public static final String SESSION_LOGIN_TIME    = "loginTime";

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest  request,
            HttpServletResponse response,
            Authentication      authentication) throws IOException {

        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();

        // ── Store user info in session for quick access ───────────────────────
        HttpSession session = request.getSession(true);
        session.setAttribute(SESSION_USER_ID,    principal.getUserId());
        session.setAttribute(SESSION_USERNAME,   principal.getUsername());
        session.setAttribute(SESSION_FULL_NAME,  principal.getFullName());
        session.setAttribute(SESSION_EMAIL,      principal.getEmail());
        session.setAttribute(SESSION_LOGIN_TIME, LocalDateTime.now().toString());

        log.info("[LOGIN] user='{}' id={} ip='{}' at={}",
            principal.getUsername(),
            principal.getUserId(),
            getClientIp(request),
            LocalDateTime.now()
        );

        // ── Determine redirect target ─────────────────────────────────────────
        String redirectUrl = determineRedirect(principal);

        clearAuthenticationAttributes(request);   // remove SPRING_SECURITY_LAST_EXCEPTION
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Role-based redirect logic
    // ─────────────────────────────────────────────────────────────────────────

    private String determineRedirect(CustomUserDetails principal) {

        // 1. User has a personally configured default dashboard
        if (principal.getDefaultDashboard() != null) {
            return switch (principal.getDefaultDashboard()) {
                case PRODUCTION  -> "/production/dashboard";
                case INVENTORY_WAREHOUSE   -> "/inventory/dashboard";
                case COMMERCIAL  -> "/commercial/dashboard";
                default          -> "/dashboard";
            };
        }

        // 2. Fall back to role-based default
        if (principal.hasRole("ROLE_SUPER_ADMIN")) return "/dashboard";
        if (principal.hasRole("ROLE_ADMIN"))       return "/dashboard";
        if (principal.hasRole("ROLE_PRODUCTION"))  return "/production/dashboard";
        if (principal.hasRole("ROLE_INVENTORY"))   return "/inventory/dashboard";
        if (principal.hasRole("ROLE_COMMERCIAL"))  return "/commercial/dashboard";

        // 3. Generic fallback
        return "/dashboard";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Extract the real client IP, respecting common reverse-proxy headers.
     */
    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        return realIp != null ? realIp : request.getRemoteAddr();
    }
}
