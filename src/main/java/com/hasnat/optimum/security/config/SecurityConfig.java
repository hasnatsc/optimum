package com.hasnat.optimum.security.config;

import com.hasnat.optimum.security.handler.*;
import com.hasnat.optimum.security.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.*;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity          // enables @PreAuthorize / @PostAuthorize on controllers
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService   userDetailsService;
    private final CustomAuthenticationSuccessHandler successHandler;
    private final CustomAuthenticationFailureHandler failureHandler;

    // ─── Password encoder ──────────────────────────────────────────────────────

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    // ─── Authentication provider ───────────────────────────────────────────────

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        provider.setHideUserNotFoundExceptions(false); // show UsernameNotFoundException
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(authenticationProvider());
    }

    // ─── Security filter chain ─────────────────────────────────────────────────

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // ── CSRF ── use cookie-based so Thymeleaf auto-includes it ──────
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                )

                // ── Authorization rules ──────────────────────────────────────────
                .authorizeHttpRequests(auth -> auth
                        // Public static assets
                        .requestMatchers(
                                "/css/**", "/js/**", "/images/**", "/fonts/**",
                                "/webjars/**", "/favicon.ico"
                        ).permitAll()

                        // Public pages
                        .requestMatchers(
                                "/login", "/login/**",
                                "/forgot-password", "/reset-password",
                                "/error"
                        ).permitAll()

                        // Actuator — restrict to SUPER_ADMIN only
                        .requestMatchers("/actuator/**").hasAuthority("ROLE_SUPER_ADMIN")

                        // Everything else requires authentication.
                        // Fine-grained permission checks are done via @PreAuthorize
                        // on individual controllers.
                        .anyRequest().authenticated()
                )

                // ── Form login ───────────────────────────────────────────────────
                .formLogin(form -> form
                        .loginPage("/login")                    // GET /login → your Thymeleaf view
                        .loginProcessingUrl("/login")           // POST /login → Spring processes
                        .usernameParameter("principal")         // <input name="principal">
                        .passwordParameter("password")          // <input name="password">
                        .successHandler(successHandler)
                        .failureHandler(failureHandler)
                        .permitAll()
                )

                // ── Remember-me (optional but useful) ────────────────────────────
                .rememberMe(rm -> rm
                        .userDetailsService(userDetailsService)
                        .tokenValiditySeconds(7 * 24 * 60 * 60)  // 7 days
                        .rememberMeParameter("remember-me")       // <input name="remember-me">
                        .key("optimum-secret-key")                // move to application.properties!
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
//                .sessionManagement(session -> session
//                        .sessionFixation(fixation ->
//                                fixation.migrateSession()
//                        )
//                        .invalidSessionUrl("/login?invalid")
//                        .maximumSessions(max -> max
//                                .maximumSessions(1)
//                                .expiredUrl("/login?expired")
//                        )
//                )

                // ── Exception handling ───────────────────────────────────────────
                .exceptionHandling(ex -> ex
                        .accessDeniedPage("/error/403")
                        .authenticationEntryPoint((req, res, authEx) ->
                                res.sendRedirect(req.getContextPath() + "/login"))
                )

                // ── Security headers ─────────────────────────────────────────────
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives(
                                        "default-src 'self'; " +
                                                "script-src 'self' 'unsafe-inline' 'unsafe-eval' cdn.jsdelivr.net; " +
                                                "style-src 'self' 'unsafe-inline' fonts.googleapis.com cdn.jsdelivr.net; " +
                                                "font-src 'self' fonts.gstatic.com cdn.jsdelivr.net; " +
                                                "img-src 'self' data:; " +
                                                "frame-ancestors 'none';"
                                )
                        )
                        .frameOptions(fo -> fo.sameOrigin())
                        .referrerPolicy(rp -> rp
                                .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000)
                        )
                );

        return http.build();
    }
}