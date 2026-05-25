package com.hasnat.optimum.security.service;

import com.hasnat.optimum.security.entity.AppMenu;
import com.hasnat.optimum.security.entity.Role;
import com.hasnat.optimum.security.entity.RoleMenuAccess;
import com.hasnat.optimum.security.repository.AppMenuRepository;
import com.hasnat.optimum.security.repository.RoleMenuAccessRepository;
import com.hasnat.optimum.security.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RoleMenuAccessServiceImpl implements RoleMenuAccessService {

    private final RoleRepository           roleRepo;
    private final AppMenuRepository        menuRepo;
    private final RoleMenuAccessRepository rmaRepo;

    // ─────────────────────────────────────────────────────────────────────────
    // GET MENU TREE WITH ACCESS FLAGS
    // Returns the full MODULE → GROUP → LEAF tree with per-node access flags
    // based on existing RoleMenuAccess entries for this role.
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getMenuTreeForRole(Long roleId) {
        Role role = findRole(roleId);

        // Map of menuId → existing access entry
        Map<Long, RoleMenuAccess> accessMap = rmaRepo.findByRole(role).stream()
                .collect(Collectors.toMap(rma -> rma.getMenu().getId(), rma -> rma));

        // All active menus in display order
        List<AppMenu> allMenus = menuRepo.findAllActiveOrdered();
        Map<Long, Map<String, Object>> nodeMap = new LinkedHashMap<>();

        // Build nodes
        for (AppMenu m : allMenus) {
            RoleMenuAccess rma = accessMap.get(m.getId());
            Map<String, Object> node = new LinkedHashMap<>();
            node.put("id",           m.getId());
            node.put("menuCode",     m.getMenuCode());
            node.put("menuName",     m.getMenuName());
            node.put("menuUrl",      m.getMenuUrl() != null ? m.getMenuUrl() : "");
            node.put("icon",         m.getIcon() != null ? m.getIcon() : "");
            node.put("menuType",     m.getMenuType().name());
            node.put("parentId",     m.getParentId());
            node.put("displayOrder", m.getDisplayOrder());
            node.put("permission",   m.getRequiredPermission() != null ? m.getRequiredPermission() : "");
            // Access flags — false when no entry exists
            node.put("canView",   rma != null && rma.isCanView());
            node.put("canCreate", rma != null && rma.isCanCreate());
            node.put("canEdit",   rma != null && rma.isCanEdit());
            node.put("canDelete", rma != null && rma.isCanDelete());
            node.put("children",  new ArrayList<>());
            nodeMap.put(m.getId(), node);
        }

        // Wire parent → children
        List<Map<String, Object>> roots = new ArrayList<>();
        for (AppMenu m : allMenus) {
            Map<String, Object> node = nodeMap.get(m.getId());
            if (m.getParentId() != null && nodeMap.containsKey(m.getParentId())) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> children =
                        (List<Map<String, Object>>) nodeMap.get(m.getParentId()).get("children");
                children.add(node);
            } else {
                roots.add(node);
            }
        }

        return roots;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SAVE — replaces the full set for a role
    // Payload: list of { menuId, canView, canCreate, canEdit, canDelete }
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void saveMenuAccess(Long roleId, List<Map<String, Object>> menuAccess) {
        Role role = findRole(roleId);

        // Clear existing entries
        rmaRepo.deleteByRole(role);

        if (menuAccess == null || menuAccess.isEmpty()) {
            log.info("[RMA] Cleared all menus for role='{}' by '{}'", role.getName(), currentUser());
            return;
        }

        // Build and resolve menu IDs
        Set<Long> menuIds = menuAccess.stream()
                .map(a -> toLong(a.get("menuId")))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, AppMenu> menuById = menuRepo.findAllById(menuIds).stream()
                .collect(Collectors.toMap(AppMenu::getId, m -> m));

        String actor = currentUser();
        List<RoleMenuAccess> entries = new ArrayList<>();

        for (Map<String, Object> a : menuAccess) {
            Long menuId = toLong(a.get("menuId"));
            if (menuId == null) continue;
            AppMenu menu = menuById.get(menuId);
            if (menu == null) continue;

            entries.add(RoleMenuAccess.builder()
                    .role(role)
                    .menu(menu)
                    .canView(toBool(a.get("canView")))
                    .canCreate(toBool(a.get("canCreate")))
                    .canEdit(toBool(a.get("canEdit")))
                    .canDelete(toBool(a.get("canDelete")))
                    .build());
        }

        rmaRepo.saveAll(entries);
        long viewable = entries.stream().filter(RoleMenuAccess::isCanView).count();
        log.info("[RMA] Saved {} entries ({} canView) for role='{}' by '{}'",
                entries.size(), viewable, role.getName(), actor);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CLEAR — reverts to permission-based filtering
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void clearMenuAccess(Long roleId) {
        Role role = findRole(roleId);
        rmaRepo.deleteByRole(role);
        log.info("[RMA] Cleared menus for role='{}' by '{}'", role.getName(), currentUser());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private Role findRole(Long id) {
        return roleRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found: " + id));
    }

    private String currentUser() {
        var a = SecurityContextHolder.getContext().getAuthentication();
        return a != null ? a.getName() : "SYSTEM";
    }

    private Long toLong(Object v) {
        if (v == null) return null;
        if (v instanceof Integer i) return i.longValue();
        if (v instanceof Long l)    return l;
        try { return Long.parseLong(v.toString()); } catch (Exception e) { return null; }
    }

    private boolean toBool(Object v) {
        if (v == null) return false;
        if (v instanceof Boolean b) return b;
        return Boolean.parseBoolean(v.toString());
    }
}
