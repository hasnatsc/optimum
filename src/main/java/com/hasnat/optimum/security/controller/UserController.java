package com.hasnat.optimum.security.controller;

import com.hasnat.optimum.security.dto.UserDTO;
import com.hasnat.optimum.security.entity.User;
import com.hasnat.optimum.security.service.UserService;
import com.hasnat.optimum.utility.DataTableResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter DT_FMT   = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    // ── Page ──────────────────────────────────────────────────────────────────

    @GetMapping
    @PreAuthorize("hasAuthority('PERM_USER_VIEW')")
    public String index() {
        return "security/user-management";
    }

    // ── DataTable ─────────────────────────────────────────────────────────────

    @GetMapping("/list")
    @ResponseBody
    @PreAuthorize("hasAuthority('PERM_USER_VIEW')")
    public DataTableResponse<Map<String, Object>> list(
            @RequestParam(defaultValue = "1")  int    draw,
            @RequestParam(defaultValue = "0")  int    start,
            @RequestParam(defaultValue = "10") int    length,
            @RequestParam(value = "search[value]", defaultValue = "") String searchValue,
            @RequestParam(required = false)    String statusFilter) {
        return userService.datatableList(draw, start, length, searchValue, statusFilter);
    }

    // ── Save (create / update) ────────────────────────────────────────────────

    @PostMapping("/save")
    @ResponseBody
    @PreAuthorize("hasAnyAuthority('PERM_USER_CREATE', 'PERM_USER_EDIT')")
    public Map<String, Object> save(@RequestBody UserDTO dto) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (dto.getId() != null) {
                userService.updateUser(dto.getId(), dto);
                response.put("success", true);
                response.put("message", "User updated successfully.");
            } else {
                userService.createUser(dto);
                response.put("success", true);
                response.put("message", "User created successfully.");
            }
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }
        return response;
    }

    // ── Show ──────────────────────────────────────────────────────────────────

    @GetMapping("/show/{id}")
    @ResponseBody
    @PreAuthorize("hasAuthority('PERM_USER_VIEW')")
    public Map<String, Object> show(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            UserDTO dto = userService.getUserById(id);
            response.put("obj",     Map.of("defaultData", convertToMap(dto)));
            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }
        return response;
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    @DeleteMapping("/delete/{id}")
    @ResponseBody
    @PreAuthorize("hasAuthority('PERM_USER_DELETE')")
    public Map<String, Object> delete(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            userService.deleteUser(id);
            response.put("success", true);
            response.put("message", "User deleted successfully.");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }
        return response;
    }

    // ── Toggle status ─────────────────────────────────────────────────────────

    @PostMapping("/toggle-status/{id}")
    @ResponseBody
    @PreAuthorize("hasAuthority('PERM_USER_TOGGLE_STATUS')")
    public Map<String, Object> toggleStatus(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            userService.toggleStatus(id);
            response.put("success", true);
            response.put("message", "User status updated.");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }
        return response;
    }

    // ── Change password (admin-initiated) ─────────────────────────────────────

    @PostMapping("/change-password/{id}")
    @ResponseBody
    @PreAuthorize("hasAuthority('PERM_USER_CHANGE_PASSWORD')")
    public Map<String, Object> changePassword(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        Map<String, Object> response = new HashMap<>();
        try {
            String newPassword = body.get("password");
            userService.changePassword(id, newPassword);
            response.put("success", true);
            response.put("message", "Password changed successfully.");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }
        return response;
    }

    // ── Select2 AJAX — roles ──────────────────────────────────────────────────

    @GetMapping("/roles/search")
    @ResponseBody
    @PreAuthorize("hasAnyAuthority('PERM_USER_CREATE', 'PERM_USER_EDIT')")
    public List<Map<String, Object>> searchRoles(
            @RequestParam(required = false) String search) {
        return userService.searchRolesForSelect2(search);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Map<String, Object> convertToMap(UserDTO dto) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id",                    dto.getId());
        map.put("username",              dto.getUsername());
        map.put("fullName",              dto.getFullName());
        map.put("email",                 dto.getEmail());
        map.put("phone",                 dto.getPhone() != null ? dto.getPhone() : "");
        map.put("enabled",               dto.isEnabled());
        map.put("accountNonExpired",     dto.isAccountNonExpired());
        map.put("accountNonLocked",      dto.isAccountNonLocked());
        map.put("credentialsNonExpired", dto.isCredentialsNonExpired());
        map.put("defaultDashboard",
            dto.getDefaultDashboard() != null ? dto.getDefaultDashboard().name() : "DEFAULT");
        map.put("roleIds",   dto.getRoleIds());
        map.put("roleNames", dto.getRoleNames());
        map.put("createdBy", dto.getCreatedBy());
        map.put("updatedBy", dto.getUpdatedBy());
        map.put("createdAt", dto.getCreatedAt() != null ? dto.getCreatedAt().format(DT_FMT) : "");
        map.put("updatedAt", dto.getUpdatedAt() != null ? dto.getUpdatedAt().format(DT_FMT) : "");
        map.put("lastLoginAt", dto.getLastLoginAt() != null
            ? dto.getLastLoginAt().format(DT_FMT) : "Never");
        return map;
    }
}
