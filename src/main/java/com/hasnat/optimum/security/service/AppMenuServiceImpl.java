package com.hasnat.optimum.security.service;

import com.hasnat.optimum.security.dto.AppMenuDTO;
import com.hasnat.optimum.security.entity.AppMenu;
import com.hasnat.optimum.security.repository.AppMenuRepository;
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

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AppMenuServiceImpl implements AppMenuService {

    private final AppMenuRepository menuRepository;
    private final JdbcTemplate      jdbcTemplate;

    private static final DateTimeFormatter DT_FMT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    // ═════════════════════════════════════════════════════════════════════
    // USER MENU — permission-filtered flat list
    // ═════════════════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getUserMenuResponse() {

        // 1. All active menus in display order (single query)
        List<AppMenu> allMenus = menuRepository.findAllActiveOrdered();

        // 2. Current user's authorities
        Set<String> authorities = getCurrentAuthorities();
        boolean isSuperAdmin    = authorities.contains("ROLE_SUPER_ADMIN");

        // 3. Build id → menu map for ancestor traversal
        Map<Long, AppMenu> menuById = allMenus.stream()
                .collect(Collectors.toMap(AppMenu::getId, m -> m));

        // 4. Find directly visible menus
        Set<Long> visibleIds = new HashSet<>();

        for (AppMenu m : allMenus) {
            boolean visible = isSuperAdmin
                    || m.getRequiredPermission() == null
                    || m.getRequiredPermission().isBlank()
                    || authorities.contains(m.getRequiredPermission());

            if (visible) {
                visibleIds.add(m.getId());

                // 5. Walk up the parentId chain — include all ancestors
                //    so the tree renders MODULE → GROUP → LEAF correctly
                Long parentId = m.getParentId();
                while (parentId != null) {
                    visibleIds.add(parentId);
                    AppMenu parent = menuById.get(parentId);
                    parentId = (parent != null) ? parent.getParentId() : null;
                }
            }
        }

        // 6. Build the response flat list (preserve displayOrder)
        List<Map<String, Object>> items = allMenus.stream()
                .filter(m -> visibleIds.contains(m.getId()))
                .map(this::toUserMenuMap)
                .collect(Collectors.toList());

        log.debug("[Menu] user='{}' sees {} of {} menu items",
                currentUsername(), items.size(), allMenus.size());

        return Map.of(
                "success", true,
                "menus",   Map.of("items", items)
        );
    }

    /** Converts an AppMenu to the flat map shape the JS tree builder expects. */
    private Map<String, Object> toUserMenuMap(AppMenu m) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id",           m.getId());
        map.put("parentMenuId", m.getParentId());       // JS uses this field name
        map.put("menuName",     m.getMenuName());
        map.put("menuUrl",      m.getMenuUrl());
        map.put("icon",         m.getIcon() != null ? m.getIcon() : "");
        map.put("target",       m.getTarget() != null ? m.getTarget() : "_self");
        map.put("displayOrder", m.getDisplayOrder());
        return map;
    }

    // ═════════════════════════════════════════════════════════════════════
    // CRUD
    // ═════════════════════════════════════════════════════════════════════

    @Override
    public AppMenuDTO createMenu(AppMenuDTO dto) {
        validateMenuName(dto.getMenuName(), dto.getParentId(), null);

        int nextOrder = menuRepository.findMaxDisplayOrderByParentId(dto.getParentId()) + 1;

        AppMenu menu = AppMenu.builder()
                .menuName(dto.getMenuName().trim())
                .icon(dto.getIcon())
                .menuUrl(dto.getMenuUrl() != null ? dto.getMenuUrl().trim() : null)
                .target(dto.getTarget() != null ? dto.getTarget() : "_self")
                .parentId(dto.getParentId())
                .menuType(dto.getMenuType() != null ? dto.getMenuType() : AppMenu.MenuType.LEAF)
                .displayOrder(dto.getDisplayOrder() != null ? dto.getDisplayOrder() : nextOrder)
                .requiredPermission(blankToNull(dto.getRequiredPermission()))
                .active(dto.isActive())
                .createdBy(currentUsername())
                .updatedBy(currentUsername())
                .build();

        return toDTO(menuRepository.save(menu));
    }

    @Override
    public AppMenuDTO updateMenu(Long id, AppMenuDTO dto) {
        AppMenu menu = findActive(id);
        validateMenuName(dto.getMenuName(), dto.getParentId(), id);

        // Prevent circular parent reference
        if (dto.getParentId() != null && dto.getParentId().equals(id)) {
            throw new IllegalArgumentException("A menu cannot be its own parent.");
        }

        menu.setMenuName(dto.getMenuName().trim());
        menu.setIcon(dto.getIcon());
        menu.setMenuUrl(dto.getMenuUrl() != null ? dto.getMenuUrl().trim() : null);
        menu.setTarget(dto.getTarget() != null ? dto.getTarget() : "_self");
        menu.setParentId(dto.getParentId());
        menu.setMenuType(dto.getMenuType() != null ? dto.getMenuType() : menu.getMenuType());
        menu.setDisplayOrder(dto.getDisplayOrder() != null ? dto.getDisplayOrder() : menu.getDisplayOrder());
        menu.setRequiredPermission(blankToNull(dto.getRequiredPermission()));
        menu.setActive(dto.isActive());
        menu.setUpdatedBy(currentUsername());

        return toDTO(menuRepository.save(menu));
    }

    @Override
    @Transactional(readOnly = true)
    public AppMenuDTO getMenuById(Long id) {
        return toDTO(findActive(id));
    }

    @Override
    public void deleteMenu(Long id) {
        AppMenu menu = findActive(id);

        // Soft-delete children first
        List<AppMenu> children = menuRepository.findByParentIdAndDeletedFalse(id);
        children.forEach(child -> {
            child.setDeleted(true);
            child.setActive(false);
            child.setUpdatedBy(currentUsername());
        });
        menuRepository.saveAll(children);

        menu.setDeleted(true);
        menu.setActive(false);
        menu.setUpdatedBy(currentUsername());
        menuRepository.save(menu);

        log.info("[Menu] Deleted id={} name='{}' by '{}'",
                id, menu.getMenuName(), currentUsername());
    }

    @Override
    public void toggleStatus(Long id) {
        AppMenu menu = findActive(id);
        menu.setActive(!menu.isActive());
        menu.setUpdatedBy(currentUsername());
        menuRepository.save(menu);
    }

    // ═════════════════════════════════════════════════════════════════════
    // DATATABLE  (JdbcTemplate — raw SQL for flexibility)
    // ═════════════════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public DataTableResponse<Map<String, Object>> datatableList(
            int draw, int start, int length,
            String searchValue, String typeFilter, String statusFilter) {

        String search = (searchValue != null && !searchValue.isBlank())
                ? "%" + searchValue.trim().toLowerCase() + "%" : null;

        StringBuilder where = new StringBuilder("""
            FROM app_menus m
            LEFT JOIN app_menus p ON p.id = m.parent_id
            WHERE m.deleted = false
            """);

        List<Object> params = new ArrayList<>();

        if (search != null) {
            where.append("""
                AND (LOWER(m.menu_name) LIKE ?
                  OR LOWER(COALESCE(m.menu_url,'')) LIKE ?
                  OR LOWER(COALESCE(m.required_permission,'')) LIKE ?)
                """);
            params.add(search); params.add(search); params.add(search);
        }
        if (typeFilter != null && !typeFilter.isBlank()) {
            where.append(" AND m.menu_type = ? ");
            params.add(typeFilter);
        }
        if ("1".equals(statusFilter)) {
            where.append(" AND m.active = true ");
        } else if ("0".equals(statusFilter)) {
            where.append(" AND m.active = false ");
        }

        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) " + where, Long.class, params.toArray());

        String sql = """
                SELECT
                    m.id,
                    m.menu_name,
                    m.menu_type,
                    m.menu_url,
                    m.icon,
                    m.display_order,
                    m.required_permission,
                    m.active,
                    COALESCE(p.menu_name, '—') AS parent_name
                """ + where + """
                ORDER BY m.display_order ASC, m.id ASC
                LIMIT ? OFFSET ?
                """;

        params.add(length);
        params.add(start);

        List<Map<String, Object>> data = jdbcTemplate.query(
                sql, params.toArray(), (rs, row) -> {
                    Map<String, Object> r = new LinkedHashMap<>();
                    long   mid    = rs.getLong("id");
                    boolean active = rs.getBoolean("active");
                    String  type   = rs.getString("menu_type");

                    r.put("sl",          start + row + 1);
                    r.put("menu_name",   rs.getString("menu_name"));
                    r.put("menu_type",   buildTypeBadge(type));
                    r.put("parent",      rs.getString("parent_name"));
                    r.put("menu_url",    nvl(rs.getString("menu_url")));
                    r.put("icon",        buildIconPreview(rs.getString("icon")));
                    r.put("order",       rs.getInt("display_order"));
                    r.put("permission",  buildPermBadge(rs.getString("required_permission")));
                    r.put("status",      active
                            ? "<span class='badge bg-success'>Active</span>"
                            : "<span class='badge bg-secondary'>Inactive</span>");
                    r.put("actions",     buildActionButtons(mid, active));
                    return r;
                });

        return DataTableResponse.of(draw, total, data);
    }

    // ═════════════════════════════════════════════════════════════════════
    // PARENT SELECT2
    // ═════════════════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getParentMenusForSelect(String search) {
        return menuRepository.findActiveParentCandidates().stream()
                .filter(m -> search == null || search.isBlank()
                        || m.getMenuName().toLowerCase().contains(search.toLowerCase()))
                .map(m -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id",       m.getId());
                    item.put("text",     m.getMenuName());
                    item.put("menuType", m.getMenuType().name());
                    return item;
                })
                .collect(Collectors.toList());
    }

    // ═════════════════════════════════════════════════════════════════════
    // HELPERS
    // ═════════════════════════════════════════════════════════════════════

    private AppMenu findActive(Long id) {
        return menuRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Menu not found: " + id));
    }

    private void validateMenuName(String name, Long parentId, Long excludeId) {
        boolean exists = excludeId == null
                ? menuRepository.existsByMenuNameAndParentIdAndDeletedFalse(name, parentId)
                : menuRepository.existsByMenuNameAndParentIdAndIdNotAndDeletedFalse(name, parentId, excludeId);
        if (exists) {
            throw new IllegalArgumentException(
                    "A menu named '" + name + "' already exists at this level.");
        }
    }

    private AppMenuDTO toDTO(AppMenu m) {
        return AppMenuDTO.builder()
                .id(m.getId())
                .menuName(m.getMenuName())
                .icon(m.getIcon())
                .menuUrl(m.getMenuUrl())
                .target(m.getTarget())
                .parentId(m.getParentId())
                .menuType(m.getMenuType())
                .displayOrder(m.getDisplayOrder())
                .requiredPermission(m.getRequiredPermission())
                .active(m.isActive())
                .createdBy(m.getCreatedBy())
                .updatedBy(m.getUpdatedBy())
                .createdAt(m.getCreatedAt())
                .updatedAt(m.getUpdatedAt())
                .build();
    }

    private Set<String> getCurrentAuthorities() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return Set.of();
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
    }

    private String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "SYSTEM";
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    private String nvl(String s) { return s != null ? s : "—"; }

    // ── Action buttons ────────────────────────────────────────────────────

    private String buildActionButtons(long id, boolean active) {
        String toggle = active
                ? "<button class='btn btn-white btn-sm' onclick='menuToggle(" + id + ")' title='Deactivate'>"
                  + "<i class='fa fa-toggle-on text-success'></i></button>"
                : "<button class='btn btn-white btn-sm' onclick='menuToggle(" + id + ")' title='Activate'>"
                  + "<i class='fa fa-toggle-off text-secondary'></i></button>";

        return "<div class='btn-group btn-group-sm'>"
                + "<button class='btn btn-white btn-sm' onclick='menuShow("   + id + ")' title='View'>"
                  + "<i class='fa-regular fas fa-book-open-reader text-success'></i></button>"
                + "<button class='btn btn-white btn-sm' onclick='menuEdit("   + id + ")' title='Edit'>"
                  + "<i class='fa-regular fa-pen-to-square text-warning'></i></button>"
                + toggle
                + "<button class='btn btn-white btn-sm' onclick='menuDelete(" + id + ")' title='Delete'>"
                  + "<i class='fa-regular fa-trash-can text-danger'></i></button>"
                + "</div>";
    }

    private String buildTypeBadge(String type) {
        if (type == null) return "";
        return switch (type) {
            case "MODULE" -> "<span class='badge bg-primary'>MODULE</span>";
            case "GROUP"  -> "<span class='badge bg-info text-dark'>GROUP</span>";
            case "LEAF"   -> "<span class='badge bg-success'>LEAF</span>";
            default       -> "<span class='badge bg-secondary'>" + type + "</span>";
        };
    }

    private String buildPermBadge(String perm) {
        if (perm == null || perm.isBlank()) {
            return "<span class='badge bg-light text-muted border'>All Users</span>";
        }
        return "<span class='badge bg-warning text-dark' title='" + perm + "'>"
                + perm.replace("PERM_", "") + "</span>";
    }

    private String buildIconPreview(String icon) {
        if (icon == null || icon.isBlank()) return "—";
        return "<i class='" + icon + " me-1'></i><small class='text-muted'>" + icon + "</small>";
    }
}
