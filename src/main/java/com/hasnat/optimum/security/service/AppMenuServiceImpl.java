package com.hasnat.optimum.security.service;

import com.hasnat.optimum.security.dto.AppMenuDTO;
import com.hasnat.optimum.security.entity.AppMenu;
import com.hasnat.optimum.security.entity.Role;
import com.hasnat.optimum.security.repository.AppMenuRepository;
import com.hasnat.optimum.security.repository.RoleMenuAccessRepository;
import com.hasnat.optimum.utility.DataTableResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AppMenuServiceImpl — menu CRUD + user-menu permission filtering.
 *
 * getUserMenuResponse() filtering (priority order):
 *   1. SUPER_ADMIN                     → see everything
 *   2. Role has canView=true entries   → PATH A: use RoleMenuAccess
 *   3. No canView entries for role     → PATH B: use AppMenu.requiredPermission
 *   4. Both paths: include ancestor chain for tree integrity
 *
 * Also respects AppMenu.visible — items with visible=false are excluded
 * even if the role has explicit access.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AppMenuServiceImpl implements AppMenuService {

    private final AppMenuRepository        menuRepo;
    private final RoleMenuAccessRepository rmaRepo;
    private final JdbcTemplate             jdbc;

    private static final DateTimeFormatter DT_FMT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    // ═════════════════════════════════════════════════════════════════════════
    // USER MENU
    // ═════════════════════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getUserMenuResponse() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return Map.of("success", false, "message", "Not authenticated");
        }

        Set<String> authorities = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        boolean isSuperAdmin = authorities.contains("ROLE_SUPER_ADMIN");

        // All active + visible + non-deleted menus (one query)
        List<AppMenu> all   = menuRepo.findAllActiveOrdered();
        Map<Long, AppMenu> byId = all.stream()
                .collect(Collectors.toMap(AppMenu::getId, m -> m));

        Set<Long> directlyVisible;

        if (isSuperAdmin) {
            directlyVisible = byId.keySet();

        } else {
            Set<Long> roleIds = extractRoleIds(auth);

            if (!roleIds.isEmpty() && rmaRepo.existsViewableByRoleIds(roleIds)) {
                // PATH A — explicit role-menu mapping (canView = true only)
                directlyVisible = rmaRepo.findViewableMenuIdsByRoleIds(roleIds);
                log.debug("[Menu] PATH-A role-based: user='{}' roles={} menus={}",
                        auth.getName(), roleIds.size(), directlyVisible.size());
            } else {
                // PATH B — permission fallback via AppMenu.requiredPermission
                directlyVisible = all.stream()
                        .filter(m -> m.getRequiredPermission() == null
                                  || m.getRequiredPermission().isBlank()
                                  || authorities.contains(m.getRequiredPermission()))
                        .map(AppMenu::getId)
                        .collect(Collectors.toSet());
                log.debug("[Menu] PATH-B permission: user='{}' menus={}",
                        auth.getName(), directlyVisible.size());
            }
        }

        // Include full ancestor chain so the JS tree connects correctly
        Set<Long> visibleIds = new HashSet<>(directlyVisible);
        for (Long mid : new HashSet<>(directlyVisible)) {
            AppMenu cursor = byId.get(mid);
            while (cursor != null && cursor.getParentId() != null) {
                visibleIds.add(cursor.getParentId());
                cursor = byId.get(cursor.getParentId());
            }
        }

        List<Map<String, Object>> items = all.stream()
                .filter(m -> visibleIds.contains(m.getId()))
                .map(this::toUserMenuMap)
                .collect(Collectors.toList());

        return Map.of("success", true, "menus", Map.of("items", items));
    }

    private Set<Long> extractRoleIds(Authentication auth) {
        if (auth.getPrincipal() instanceof CustomUserDetails cd) {
            return cd.getUser().getRoles().stream()
                    .map(Role::getId).collect(Collectors.toSet());
        }
        return Set.of();
    }

    private Map<String, Object> toUserMenuMap(AppMenu m) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("id",           m.getId());
        r.put("parentMenuId", m.getParentId());   // JS buildMenuTree() key
        r.put("menuName",     m.getMenuName());
        r.put("menuCode",     m.getMenuCode());
        r.put("menuUrl",      m.getMenuUrl());
        r.put("icon",         nvl(m.getIcon()));
        r.put("target",       m.getTarget() != null ? m.getTarget() : "_self");
        r.put("displayOrder", m.getDisplayOrder());
        return r;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // CRUD
    // ═════════════════════════════════════════════════════════════════════════

    @Override
    public AppMenuDTO createMenu(AppMenuDTO dto) {
        validateMenuCode(dto.getMenuCode(), null);
        validateMenuName(dto.getMenuName(), dto.getParentId(), null);

        int nextOrder = menuRepo.findMaxDisplayOrderByParentId(dto.getParentId()) + 1;

        AppMenu m = AppMenu.builder()
                .menuCode(dto.getMenuCode().trim().toUpperCase())
                .menuName(dto.getMenuName().trim())
                .description(dto.getDescription())
                .icon(dto.getIcon())
                .menuUrl(blank(dto.getMenuUrl()))
                .target(nvl(dto.getTarget(), "_self"))
                .parentId(dto.getParentId())
                .menuType(dto.getMenuType() != null ? dto.getMenuType() : AppMenu.MenuType.LEAF)
                .displayOrder(dto.getDisplayOrder() != null ? dto.getDisplayOrder() : nextOrder)
                .moduleName(blank(dto.getModuleName()))
                .requiredPermission(blank(dto.getRequiredPermission()))
                .visible(dto.isVisible())
                .active(dto.isActive())
                .createdBy(currentUser())
                .updatedBy(currentUser())
                .build();

        return toDTO(menuRepo.save(m));
    }

    @Override
    public AppMenuDTO updateMenu(Long id, AppMenuDTO dto) {
        AppMenu m = findActive(id);

        validateMenuCode(dto.getMenuCode(), id);
        validateMenuName(dto.getMenuName(), dto.getParentId(), id);

        if (dto.getParentId() != null && dto.getParentId().equals(id)) {
            throw new IllegalArgumentException("A menu cannot be its own parent.");
        }

        m.setMenuCode(dto.getMenuCode().trim().toUpperCase());
        m.setMenuName(dto.getMenuName().trim());
        m.setDescription(dto.getDescription());
        m.setIcon(dto.getIcon());
        m.setMenuUrl(blank(dto.getMenuUrl()));
        m.setTarget(nvl(dto.getTarget(), "_self"));
        m.setParentId(dto.getParentId());
        m.setMenuType(dto.getMenuType() != null ? dto.getMenuType() : m.getMenuType());
        m.setDisplayOrder(dto.getDisplayOrder() != null ? dto.getDisplayOrder() : m.getDisplayOrder());
        m.setModuleName(blank(dto.getModuleName()));
        m.setRequiredPermission(blank(dto.getRequiredPermission()));
        m.setVisible(dto.isVisible());
        m.setActive(dto.isActive());
        m.setUpdatedBy(currentUser());

        return toDTO(menuRepo.save(m));
    }

    @Override
    @Transactional(readOnly = true)
    public AppMenuDTO getMenuById(Long id) { return toDTO(findActive(id)); }

    @Override
    public void deleteMenu(Long id) {
        AppMenu m = findActive(id);
        menuRepo.findByParentIdAndDeletedFalse(id).forEach(child -> {
            child.setDeleted(true); child.setActive(false); child.setUpdatedBy(currentUser());
        });
        m.setDeleted(true); m.setActive(false); m.setUpdatedBy(currentUser());
        menuRepo.save(m);
    }

    @Override
    public void toggleStatus(Long id) {
        AppMenu m = findActive(id);
        m.setActive(!m.isActive()); m.setUpdatedBy(currentUser());
        menuRepo.save(m);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // DATATABLE
    // ═════════════════════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public DataTableResponse<Map<String, Object>> datatableList(
            int draw, int start, int length,
            String searchValue, String typeFilter, String statusFilter) {

        String search = (searchValue != null && !searchValue.isBlank())
                ? "%" + searchValue.trim().toLowerCase() + "%" : null;

        StringBuilder w = new StringBuilder(
            "FROM app_menus m LEFT JOIN app_menus p ON p.id = m.parent_id " +
            "WHERE m.deleted = false ");
        List<Object> p = new ArrayList<>();

        if (search != null) {
            w.append("AND (LOWER(m.menu_name) LIKE ? OR LOWER(COALESCE(m.menu_code,'')) LIKE ? " +
                     "OR LOWER(COALESCE(m.menu_url,'')) LIKE ?) ");
            p.add(search); p.add(search); p.add(search);
        }
        if (typeFilter   != null && !typeFilter.isBlank())   { w.append("AND m.menu_type=? ");   p.add(typeFilter); }
        if ("1".equals(statusFilter)) { w.append("AND m.active=true "); }
        if ("0".equals(statusFilter)) { w.append("AND m.active=false "); }

        Long total = jdbc.queryForObject("SELECT COUNT(*) " + w, Long.class, p.toArray());

        String sql = "SELECT m.id, m.menu_code, m.menu_name, m.menu_type, m.menu_url, " +
                     "m.icon, m.display_order, m.required_permission, m.visible, m.active, " +
                     "COALESCE(p.menu_name,'—') AS parent_name " + w +
                     "ORDER BY m.display_order, m.id LIMIT ? OFFSET ?";
        p.add(length); p.add(start);

        List<Map<String, Object>> data = jdbc.query(sql, p.toArray(), (rs, row) -> {
            Map<String, Object> r = new LinkedHashMap<>();
            long   mid    = rs.getLong("id");
            boolean active = rs.getBoolean("active");
            boolean visible= rs.getBoolean("visible");
            String  type   = rs.getString("menu_type");
            r.put("sl",         start + row + 1);
            r.put("menu_code",  rs.getString("menu_code"));
            r.put("menu_name",  rs.getString("menu_name"));
            r.put("menu_type",  typeBadge(type));
            r.put("parent",     rs.getString("parent_name"));
            r.put("menu_url",   nvl(rs.getString("menu_url")));
            r.put("icon",       iconPreview(rs.getString("icon")));
            r.put("order",      rs.getInt("display_order"));
            r.put("permission", permBadge(rs.getString("required_permission")));
            r.put("status",     statusBadge(active, visible));
            r.put("actions",    actions(mid, active));
            return r;
        });

        return DataTableResponse.of(draw, total, data);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // PARENT SELECT2
    // ═════════════════════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getParentMenusForSelect(String search) {
        return menuRepo.findActiveParentCandidates().stream()
                .filter(m -> search == null || search.isBlank()
                        || m.getMenuName().toLowerCase().contains(search.toLowerCase()))
                .map(m -> {
                    Map<String, Object> r = new LinkedHashMap<>();
                    r.put("id",       m.getId());
                    r.put("text",     m.getMenuName());
                    r.put("menuType", m.getMenuType().name());
                    return r;
                })
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────────────

    private AppMenu findActive(Long id) {
        return menuRepo.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Menu not found: " + id));
    }

    private void validateMenuCode(String code, Long excludeId) {
        if (code == null || code.isBlank())
            throw new IllegalArgumentException("Menu code is required.");
        boolean dup = excludeId == null
                ? menuRepo.existsByMenuCode(code.trim().toUpperCase())
                : menuRepo.existsByMenuCodeAndIdNot(code.trim().toUpperCase(), excludeId);
        if (dup) throw new IllegalArgumentException("Menu code '" + code + "' already exists.");
    }

    private void validateMenuName(String name, Long parentId, Long excludeId) {
        boolean dup = excludeId == null
                ? menuRepo.existsByMenuNameAndParentIdAndDeletedFalse(name, parentId)
                : menuRepo.existsByMenuNameAndParentIdAndIdNotAndDeletedFalse(name, parentId, excludeId);
        if (dup) throw new IllegalArgumentException("Name '" + name + "' already exists at this level.");
    }

    private AppMenuDTO toDTO(AppMenu m) {
        AppMenuDTO d = new AppMenuDTO();
        d.setId(m.getId());
        d.setMenuCode(m.getMenuCode());
        d.setMenuName(m.getMenuName());
        d.setDescription(m.getDescription());
        d.setIcon(m.getIcon());
        d.setMenuUrl(m.getMenuUrl());
        d.setTarget(m.getTarget());
        d.setParentId(m.getParentId());
        d.setMenuType(m.getMenuType());
        d.setDisplayOrder(m.getDisplayOrder());
        d.setModuleName(m.getModuleName());
        d.setRequiredPermission(m.getRequiredPermission());
        d.setVisible(m.isVisible());
        d.setActive(m.isActive());
        d.setCreatedBy(m.getCreatedBy());
        d.setUpdatedBy(m.getUpdatedBy());
        d.setCreatedAt(m.getCreatedAt());
        d.setUpdatedAt(m.getUpdatedAt());
        return d;
    }

    private String currentUser() {
        var a = SecurityContextHolder.getContext().getAuthentication();
        return a != null ? a.getName() : "SYSTEM";
    }

    private String nvl(String s)                { return s != null ? s : ""; }
    private String nvl(String s, String def)    { return (s != null && !s.isBlank()) ? s : def; }
    private String blank(String s)              { return (s == null || s.isBlank()) ? null : s.trim(); }

    private String typeBadge(String t) {
        if (t == null) return "";
        return switch (t) {
            case "MODULE" -> "<span class='badge bg-primary'>MODULE</span>";
            case "GROUP"  -> "<span class='badge bg-info text-dark'>GROUP</span>";
            case "LEAF"   -> "<span class='badge bg-success'>LEAF</span>";
            default       -> "<span class='badge bg-secondary'>" + t + "</span>";
        };
    }
    private String permBadge(String p) {
        if (p == null || p.isBlank())
            return "<span class='badge bg-light text-muted border'>All Users</span>";
        return "<span class='badge bg-warning text-dark' title='" + p + "'>"
                + p.replace("PERM_", "") + "</span>";
    }
    private String statusBadge(boolean active, boolean visible) {
        if (!active)   return "<span class='badge bg-secondary'>Inactive</span>";
        if (!visible)  return "<span class='badge bg-warning text-dark'>Hidden</span>";
        return "<span class='badge bg-success'>Active</span>";
    }
    private String iconPreview(String icon) {
        if (icon == null || icon.isBlank()) return "—";
        return "<i class='" + icon + " me-1'></i><small class='text-muted'>" + icon + "</small>";
    }
    private String actions(long id, boolean active) {
        String toggle = active
                ? "<button class='btn btn-white btn-sm' onclick='menuToggle(" + id + ")' title='Deactivate'><i class='fa fa-toggle-on text-success'></i></button>"
                : "<button class='btn btn-white btn-sm' onclick='menuToggle(" + id + ")' title='Activate'><i class='fa fa-toggle-off text-secondary'></i></button>";
        return "<div class='btn-group btn-group-sm'>"
                + "<button class='btn btn-white btn-sm' onclick='menuShow("   + id + ")'><i class='fa-regular fas fa-book-open-reader text-success'></i></button>"
                + "<button class='btn btn-white btn-sm' onclick='menuEdit("   + id + ")'><i class='fa-regular fa-pen-to-square text-warning'></i></button>"
                + toggle
                + "<button class='btn btn-white btn-sm' onclick='menuDelete(" + id + ")'><i class='fa-regular fa-trash-can text-danger'></i></button>"
                + "</div>";
    }
}
