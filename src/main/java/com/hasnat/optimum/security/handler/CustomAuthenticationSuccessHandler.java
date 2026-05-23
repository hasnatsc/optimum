package com.hasnat.optimum.security.handler;

import com.hasnat.optimum.security.entity.User;
import com.hasnat.optimum.security.repository.UserRepository;
import com.hasnat.optimum.security.service.CustomUserDetails;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;

    /** Maps DefaultDashboard enum → URL */
    private static final Map<User.DefaultDashboard, String> DASHBOARD_URLS = Map.ofEntries(
            Map.entry(User.DefaultDashboard.CORE_SECURITY,               "/dashboard/security"),
            Map.entry(User.DefaultDashboard.ACCESS_MENU,                 "/dashboard/access"),
            Map.entry(User.DefaultDashboard.HRM,                         "/dashboard/hrm"),
            Map.entry(User.DefaultDashboard.SALES_CUSTOMER_OPERATIONS,   "/dashboard/sales"),
            Map.entry(User.DefaultDashboard.PURCHASE_SUPPLIER,           "/dashboard/purchase"),
            Map.entry(User.DefaultDashboard.INVENTORY_WAREHOUSE,         "/dashboard/inventory"),
            Map.entry(User.DefaultDashboard.FINANCE_ACCOUNTS,            "/dashboard/finance"),
            Map.entry(User.DefaultDashboard.PRODUCTION,                  "/dashboard/production"),
            Map.entry(User.DefaultDashboard.PRODUCT_CATALOG_ECOMMERCE,   "/dashboard/ecommerce"),
            Map.entry(User.DefaultDashboard.POS,                         "/dashboard/pos"),
            Map.entry(User.DefaultDashboard.CRM,                         "/dashboard/crm"),
            Map.entry(User.DefaultDashboard.COMMUNICATION_NOTIFICATION,  "/dashboard/notifications"),
            Map.entry(User.DefaultDashboard.COMMERCIAL,                  "/dashboard/commercial"),
            Map.entry(User.DefaultDashboard.REPORTS_ANALYTICS,           "/dashboard/reports")
    );

    @Override
    public void onAuthenticationSuccess(HttpServletRequest req,
                                        HttpServletResponse res,
                                        Authentication auth) throws IOException {
        CustomUserDetails details = (CustomUserDetails) auth.getPrincipal();

        // Update last login timestamp
        userRepository.findById(details.getUserId()).ifPresent(u -> {
            u.setLastLoginAt(LocalDateTime.now());
            userRepository.save(u);
        });

        // Redirect to dashboard
        String target = DASHBOARD_URLS.getOrDefault(
                details.getDefaultDashboard(), "/dashboard");
        res.sendRedirect(req.getContextPath() + target);
    }
}