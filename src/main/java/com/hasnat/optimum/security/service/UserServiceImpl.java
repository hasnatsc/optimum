package com.hasnat.optimum.security.service;

import com.hasnat.optimum.security.dto.UserDTO;
import com.hasnat.optimum.security.entity.*;
import com.hasnat.optimum.security.repository.*;
import com.hasnat.optimum.utility.DataTableResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository       userRepository;
    private final RoleRepository       roleRepository;
    private final PasswordEncoder      passwordEncoder;
    private final JdbcTemplate         jdbcTemplate;

    private static final DateTimeFormatter DT_FMT =
        DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    // ─────────────────────────────────────────────────────────────────────────
    // CREATE
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public UserDTO createUser(UserDTO dto) {
        // Uniqueness validation
        if (userRepository.existsByUsernameAndDeletedFalse(dto.getUsername())) {
            throw new IllegalArgumentException("Username '" + dto.getUsername() + "' is already taken.");
        }
        if (userRepository.existsByEmailAndDeletedFalse(dto.getEmail())) {
            throw new IllegalArgumentException("Email '" + dto.getEmail() + "' is already registered.");
        }
        if (dto.getPassword() == null || dto.getPassword().length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters.");
        }

        Set<Role> roles = resolveRoles(dto.getRoleIds());

        User user = User.builder()
            .username(dto.getUsername().trim().toLowerCase())
            .fullName(dto.getFullName())
            .email(dto.getEmail().trim().toLowerCase())
            .phone(dto.getPhone())
            .password(passwordEncoder.encode(dto.getPassword()))
            .enabled(dto.isEnabled())
            .accountNonExpired(true)
            .accountNonLocked(true)
            .credentialsNonExpired(true)
            .defaultDashboard(dto.getDefaultDashboard() != null
                ? dto.getDefaultDashboard() : User.DefaultDashboard.DEFAULT)
            .roles(roles)
            .createdBy(currentUsername())
            .updatedBy(currentUsername())
            .build();

        return mapToDTO(userRepository.save(user));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UPDATE
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public UserDTO updateUser(Long id, UserDTO dto) {
        User user = findActive(id);

        // Uniqueness checks excluding self
        if (userRepository.existsByUsernameAndIdNotAndDeletedFalse(dto.getUsername(), id)) {
            throw new IllegalArgumentException("Username '" + dto.getUsername() + "' is already taken.");
        }
        if (userRepository.existsByEmailAndIdNotAndDeletedFalse(dto.getEmail(), id)) {
            throw new IllegalArgumentException("Email '" + dto.getEmail() + "' is already registered.");
        }

        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail().trim().toLowerCase());
        user.setPhone(dto.getPhone());
        user.setDefaultDashboard(dto.getDefaultDashboard() != null
            ? dto.getDefaultDashboard() : User.DefaultDashboard.DEFAULT);
        user.setEnabled(dto.isEnabled());
        user.setRoles(resolveRoles(dto.getRoleIds()));
        user.setUpdatedBy(currentUsername());

        // Password update only if a new one was supplied
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            if (dto.getPassword().length() < 8) {
                throw new IllegalArgumentException("New password must be at least 8 characters.");
            }
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
            user.setCredentialsNonExpired(true);
        }

        return mapToDTO(userRepository.save(user));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // READ
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        return mapToDTO(findActive(id));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE (soft)
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void deleteUser(Long id) {
        User user = findActive(id);

        // Guard: cannot delete the last active super-admin
        boolean isSuperAdmin = user.getRoles().stream()
            .anyMatch(r -> r.getMasterRole() == MasterRole.ROLE_SUPER_ADMIN);
        if (isSuperAdmin) {
            long activeSuperAdmins = userRepository.findAll().stream()
                .filter(u -> !u.isDeleted() && u.getRoles().stream()
                    .anyMatch(r -> r.getMasterRole() == MasterRole.ROLE_SUPER_ADMIN))
                .count();
            if (activeSuperAdmins <= 1) {
                throw new IllegalStateException(
                    "Cannot delete the last Super Administrator account.");
            }
        }

        user.setDeleted(true);
        user.setEnabled(false);
        user.setUpdatedBy(currentUsername());
        userRepository.save(user);

        log.info("[USER] Soft-deleted user '{}' by '{}'", user.getUsername(), currentUsername());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TOGGLE STATUS
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void toggleStatus(Long id) {
        User user = findActive(id);
        user.setEnabled(!user.isEnabled());
        user.setUpdatedBy(currentUsername());
        userRepository.save(user);
        log.info("[USER] Toggled user '{}' enabled → {} by '{}'",
            user.getUsername(), user.isEnabled(), currentUsername());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CHANGE PASSWORD  (admin-initiated reset)
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void changePassword(Long id, String newPassword) {
        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters.");
        }
        User user = findActive(id);
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setCredentialsNonExpired(true);
        user.setUpdatedBy(currentUsername());
        userRepository.save(user);
        log.info("[USER] Password changed for user '{}' by '{}'",
            user.getUsername(), currentUsername());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DATATABLE  (server-side, JdbcTemplate)
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public DataTableResponse<Map<String, Object>> datatableList(int draw, int start, int length, String searchValue, String statusFilter) {

        String searchPattern = "%" + (searchValue != null ? searchValue.trim() : "") + "%";

        StringBuilder where = new StringBuilder("""
            FROM sec_users u
            LEFT JOIN sec_user_roles ur ON ur.user_id = u.id
            LEFT JOIN sec_roles r       ON r.id        = ur.role_id
            WHERE u.deleted = false
            """);

        List<Object> params = new ArrayList<>();

        // Status filter: "1" = active, "0" = inactive
        if ("1".equals(statusFilter)) {
            where.append(" AND u.is_enabled = true ");
        } else if ("0".equals(statusFilter)) {
            where.append(" AND u.is_enabled = false ");
        }

        if (searchValue != null && !searchValue.isBlank()) {
            where.append("""
                AND (
                    u.username  ILIKE ?
                 OR u.full_name ILIKE ?
                 OR u.email     ILIKE ?
                 OR u.phone     ILIKE ?
                )
                """);
            for (int i = 0; i < 4; i++) params.add(searchPattern);
        }

        // COUNT
        Long total = jdbcTemplate.queryForObject(
            "SELECT COUNT(DISTINCT u.id) " + where, Long.class, params.toArray());

        // DATA
        String sql = """
            SELECT
                u.id,
                u.username,
                u.full_name,
                u.email,
                u.phone,
                u.is_enabled,
                u.is_account_non_locked,
                u.default_dashboard,
                TO_CHAR(u.created_at, 'DD-MM-YYYY HH24:MI') AS created_at,
                STRING_AGG(r.name, ', ' ORDER BY r.name)    AS role_names
            """ + where + """
            GROUP BY u.id, u.username, u.full_name, u.email,
                     u.phone, u.is_enabled, u.is_account_non_locked,
                     u.default_dashboard, u.created_at
            ORDER BY u.id DESC
            LIMIT ? OFFSET ?
            """;

        params.add(length);
        params.add(start);

        List<Map<String, Object>> data = jdbcTemplate.query(sql, params.toArray(), (rs, rowNum) -> {
            Map<String, Object> row = new LinkedHashMap<>();
            long   uid      = rs.getLong("id");
            boolean enabled = rs.getBoolean("is_enabled");
            boolean locked  = !rs.getBoolean("is_account_non_locked");

            row.put("sl",        start + rowNum + 1);
            row.put("username",  rs.getString("username"));
            row.put("full_name", nvl(rs.getString("full_name")));
            row.put("email",     rs.getString("email"));
            row.put("phone",     nvl(rs.getString("phone")));
            row.put("roles",     nvl(rs.getString("role_names"), "No Role"));
            row.put("status",    buildStatusBadge(enabled, locked));
            row.put("actions",   buildActionButtons(uid, enabled));
            return row;
        });

        return DataTableResponse.<Map<String, Object>>builder()
            .draw(draw)
            .recordsTotal(total)
            .recordsFiltered(total)
            .data(data)
            .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SELECT2 — roles search
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> searchRolesForSelect2(String search) {
        return roleRepository.searchRoles(search == null ? "" : search)
            .stream()
            .map(r -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id",   r.getId());
                m.put("text", r.getName());
                m.put("masterRole", r.getMasterRole().name());
                return m;
            })
            .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MAPPING
    // ─────────────────────────────────────────────────────────────────────────

    private UserDTO mapToDTO(User user) {
        String roleNames = user.getRoles().stream()
            .map(Role::getName)
            .sorted()
            .collect(Collectors.joining(", "));

        List<Long> roleIds = user.getRoles().stream()
            .map(Role::getId)
            .collect(Collectors.toList());

        return UserDTO.builder()
            .id(user.getId())
            .username(user.getUsername())
            .fullName(user.getFullName())
            .email(user.getEmail())
            .phone(user.getPhone())
            .enabled(user.isEnabled())
            .accountNonExpired(user.isAccountNonExpired())
            .accountNonLocked(user.isAccountNonLocked())
            .credentialsNonExpired(user.isCredentialsNonExpired())
            .defaultDashboard(user.getDefaultDashboard())
            .roleIds(roleIds)
            .roleNames(roleNames)
            .createdBy(user.getCreatedBy())
            .updatedBy(user.getUpdatedBy())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .lastLoginAt(user.getLastLoginAt())
            .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private User findActive(Long id) {
        return userRepository.findActiveById(id)
            .orElseThrow(() -> new RuntimeException("User not found: " + id));
    }

    private Set<Role> resolveRoles(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) return new LinkedHashSet<>();
        return new LinkedHashSet<>(roleRepository.findAllById(roleIds));
    }

    private String currentUsername() {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            return (auth != null && auth.isAuthenticated()) ? auth.getName() : "SYSTEM";
        } catch (Exception e) {
            return "SYSTEM";
        }
    }

    private String buildStatusBadge(boolean enabled, boolean locked) {
        if (locked)   return "<span class='badge bg-danger'>Locked</span>";
        if (!enabled) return "<span class='badge bg-secondary'>Disabled</span>";
        return "<span class='badge bg-success'>Active</span>";
    }

    private String buildActionButtons(long id, boolean enabled) {
        String toggle = enabled
            ? "<button class='btn btn-white btn-sm' onclick='dataToggle(" + id + ")' title='Disable'>"
              + "<i class='fa fa-toggle-on text-success'></i></button>"
            : "<button class='btn btn-white btn-sm' onclick='dataToggle(" + id + ")' title='Enable'>"
              + "<i class='fa fa-toggle-off text-secondary'></i></button>";

        return "<div class='btn-group btn-group-sm'>" +
            "<button class='btn btn-white btn-sm' onclick='dataShow("    + id + ")' title='View'>"
                + "<i class='fa-regular fas fa-book-open-reader text-success'></i></button>" +
            "<button class='btn btn-white btn-sm' onclick='dataEdit("    + id + ")' title='Edit'>"
                + "<i class='fa-regular fa-pen-to-square text-warning'></i></button>" +
            toggle +
            "<button class='btn btn-white btn-sm' onclick='dataPwd("     + id + ")' title='Change Password'>"
                + "<i class='fa fa-key text-info'></i></button>" +
            "<button class='btn btn-white btn-sm' onclick='dataDelete("  + id + ")' title='Delete'>"
                + "<i class='fa-regular fa-trash-can text-danger'></i></button>" +
            "</div>";
    }

    private String nvl(String val) { return val != null ? val : "—"; }
    private String nvl(String val, String fallback) {
        return (val != null && !val.isBlank()) ? val : fallback;
    }
}
