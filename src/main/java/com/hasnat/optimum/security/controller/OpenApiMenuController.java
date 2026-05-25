package com.hasnat.optimum.security.controller;

import com.hasnat.optimum.security.service.AppMenuService;
import com.hasnat.optimum.security.service.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.hasnat.optimum.security.repository.UserRepository;

import java.util.Map;

/**
 * Open API endpoints — accessible by any authenticated user.
 * No @PreAuthorize needed; SecurityConfig covers them with .anyRequest().authenticated().
 *
 * Endpoints:
 *   GET  /openApi/menus/user-menu         → navigation menu filtered by user permissions
 *   POST /openApi/menus/change-password   → self-service password change
 */
@Slf4j
@RestController
@RequestMapping("/openApi/menus")
@RequiredArgsConstructor
public class OpenApiMenuController {

    private final AppMenuService menuService;
    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;

    // ─────────────────────────────────────────────────────────────────────
    // GET /openApi/menus/user-menu
    //
    // Called by loadDynamicTopMenu() in every page's topMenu fragment.
    // Returns the permission-filtered flat menu list.
    //
    // Response shape consumed by the JS buildMenuTree():
    // {
    //   "success": true,
    //   "menus": {
    //     "items": [
    //       {
    //         "id": 1,
    //         "parentMenuId": null,
    //         "menuName": "Production",
    //         "icon": "fa fa-industry",
    //         "menuUrl": null,
    //         "target": "_self",
    //         "displayOrder": 1
    //       },
    //       {
    //         "id": 5,
    //         "parentMenuId": 1,
    //         "menuName": "Production Orders",
    //         "menuUrl": "/production-orders",
    //         ...
    //       }
    //     ]
    //   }
    // }
    // ─────────────────────────────────────────────────────────────────────

    @GetMapping("/user-menu")
    public ResponseEntity<Map<String, Object>> getUserMenu() {
        try {
            return ResponseEntity.ok(menuService.getUserMenuResponse());
        } catch (Exception e) {
            log.error("[Menu] Failed to build user menu", e);
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Failed to load menu: " + e.getMessage()
            ));
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // POST /openApi/menus/change-password
    //
    // Self-service password change from the navbar "Change Password" dialog.
    // The user changes their OWN password — no admin privilege needed.
    //
    // Body: { "password": "NewPass@123" }
    // ─────────────────────────────────────────────────────────────────────

    @PostMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody Map<String, String> body) {

        String newPassword = body.get("password");

        if (newPassword == null || newPassword.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Password is required."
            ));
        }
        if (newPassword.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Password must be at least 6 characters."
            ));
        }

        try {
            userRepository.findActiveById(principal.getUserId()).ifPresent(user -> {
                user.setPassword(passwordEncoder.encode(newPassword));
                user.setCredentialsNonExpired(true);
                user.setUpdatedBy(principal.getUsername());
                userRepository.save(user);
            });

            log.info("[Menu] Password changed for user='{}'", principal.getUsername());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Password updated successfully."
            ));

        } catch (Exception e) {
            log.error("[Menu] change-password failed for user='{}'", principal.getUsername(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Failed to update password."
            ));
        }
    }
}
