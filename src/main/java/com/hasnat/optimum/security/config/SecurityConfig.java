package com.hasnat.optimum.security.config;

import com.hasnat.optimum.security.handler.CustomAuthenticationFailureHandler;
import com.hasnat.optimum.security.handler.CustomAuthenticationSuccessHandler;
import com.hasnat.optimum.security.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity   // enables @PreAuthorize / @PostAuthorize on controllers
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService            userDetailsService;
    private final CustomAuthenticationSuccessHandler  successHandler;
    private final CustomAuthenticationFailureHandler  failureHandler;

    // ── Read from application.properties ─────────────────────────────────────
    @Value("${app.security.remember-me-key}")
    private String rememberMeKey;

    // ─────────────────────────────────────────────────────────────────────────
    // Password encoder
    // ─────────────────────────────────────────────────────────────────────────

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Authentication provider
    // ─────────────────────────────────────────────────────────────────────────

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        provider.setHideUserNotFoundExceptions(false);  // surface UsernameNotFoundException
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(authenticationProvider());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FIX: HttpSessionEventPublisher — REQUIRED for concurrent session control.
    // Without this bean, Spring Security cannot detect when sessions are
    // destroyed and maximumSessions() enforcement will not work correctly.
    // ─────────────────────────────────────────────────────────────────────────

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Security filter chain
    // ─────────────────────────────────────────────────────────────────────────

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http

            // ── CSRF ──────────────────────────────────────────────────────────
            // CookieCsrfTokenRepository puts the token in an XSRF-TOKEN cookie
            // that JavaScript can read (httpOnly = false) and send back via
            // X-XSRF-TOKEN header or _csrf form field.
            // CsrfTokenRequestAttributeHandler makes it available as a request
            // attribute so Thymeleaf th:action can embed it automatically.
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
            )

            // ── Authorization rules ──────────────────────────────────────────
            .authorizeHttpRequests(auth -> auth

                // Public static assets
                .requestMatchers(
                    "/css/**", "/js/**", "/img/**", "/images/**",
                    "/fonts/**", "/webjars/**", "/favicon.ico", "/favicon.svg"
                ).permitAll()

                // Public auth pages + reset flow
                .requestMatchers(
                    "/login", "/login/**",
                    "/forgot-password",
                    "/reset-password",
                    "/auth/forgot-password",
                    "/auth/validate-reset-token",
                    "/auth/reset-password",
                    "/error", "/error/**"
                ).permitAll()

                // Actuator — SUPER_ADMIN only
                .requestMatchers("/actuator/**").hasAuthority("ROLE_SUPER_ADMIN")

                // Everything else requires authentication.
                // Fine-grained permission checks via @PreAuthorize on controllers.
                .anyRequest().authenticated()
            )

            // ── Form login ───────────────────────────────────────────────────
            .formLogin(form -> form
                .loginPage("/login")                // GET /login → Thymeleaf view
                .loginProcessingUrl("/login")       // POST /login → Spring processes
                .usernameParameter("principal")     // <input name="principal"> in form
                .passwordParameter("password")      // <input name="password"> in form
                .successHandler(successHandler)
                .failureHandler(failureHandler)
                .permitAll()
            )

            // ── Remember-me ──────────────────────────────────────────────────
            .rememberMe(rm -> rm
                .userDetailsService(userDetailsService)
                .tokenValiditySeconds(7 * 24 * 60 * 60)  // 7 days
                .rememberMeParameter("remember-me")       // <input name="remember-me">
                .key(rememberMeKey)                       // from application.properties
            )

            // ── Logout ───────────────────────────────────────────────────────
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID", "remember-me")
                .clearAuthentication(true)
                .permitAll()
            )

            // ── Session management ───────────────────────────────────────────
            .sessionManagement(session -> session
                .sessionFixation(sf -> sf.migrateSession())  // new session ID on login
                .invalidSessionUrl("/login?invalid")

                // FIX: Spring Security 6.x API — use sessionConcurrency(), NOT
                // .maximumSessions(lambda). The old chained builder was replaced.
                .sessionConcurrency(sc -> sc
                    .maximumSessions(1)              // only 1 active session per user
                    .expiredUrl("/login?expired")    // redirect when second login kicks out first
                )
            )

            // ── Exception handling ───────────────────────────────────────────
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/error/403")
                .authenticationEntryPoint((req, res, authEx) ->
                    res.sendRedirect(req.getContextPath() + "/login")
                )
            )

            // ── Security response headers ────────────────────────────────────
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives(
                        "default-src 'self'; " +
                        "script-src 'self' 'unsafe-inline' 'unsafe-eval' cdn.jsdelivr.net cdnjs.cloudflare.com; " +
                        "style-src 'self' 'unsafe-inline' fonts.googleapis.com cdn.jsdelivr.net; " +
                        "font-src 'self' fonts.gstatic.com cdn.jsdelivr.net; " +
                        "img-src 'self' data:; " +
                        "frame-ancestors 'none';"
                    )
                )
                .frameOptions(fo -> fo.sameOrigin())
                .referrerPolicy(rp -> rp
                    .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                )
                .httpStrictTransportSecurity(hsts -> hsts
                    .includeSubDomains(true)
                    .maxAgeInSeconds(31_536_000)
                )
            );

        return http.build();
    }
}
