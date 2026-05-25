package com.hasnat.optimum.security.controller;

import com.hasnat.optimum.security.service.RoleMenuAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/admin/role-menu-access")
@RequiredArgsConstructor
public class RoleMenuAccessController {

    private final RoleMenuAccessService rmaService;

    /** Assignment page — linked from Role DataTable "Assign Menus" button. */
    @GetMapping("/{roleId}")
    @PreAuthorize("hasAuthority('PERM_ROLE_EDIT')")
    public String page(@PathVariable Long roleId, Model model) {
        model.addAttribute("roleId", roleId);
        return "security/role-menu-access";
    }

    /** Tree with per-node access flags for a role. */
    @GetMapping("/{roleId}/menus")
    @ResponseBody
    @PreAuthorize("hasAuthority('PERM_ROLE_EDIT')")
    public Map<String, Object> getTree(@PathVariable Long roleId) {
        Map<String, Object> res = new HashMap<>();
        try {
            res.put("success",  true);
            res.put("menuTree", rmaService.getMenuTreeForRole(roleId));
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", "Error: " + e.getMessage());
        }
        return res;
    }

    /**
     * Save the complete access set for a role.
     * Body: { "menuAccess": [ { "menuId":1, "canView":true, "canCreate":false, ... } ] }
     */
    @PostMapping("/{roleId}/save")
    @ResponseBody
    @PreAuthorize("hasAuthority('PERM_ROLE_EDIT')")
    public Map<String, Object> save(
            @PathVariable Long roleId,
            @RequestBody  Map<String, Object> body) {

        Map<String, Object> res = new HashMap<>();
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> menuAccess =
                    (List<Map<String, Object>>) body.get("menuAccess");

            rmaService.saveMenuAccess(roleId, menuAccess);

            long viewCount = menuAccess != null
                    ? menuAccess.stream().filter(a -> Boolean.TRUE.equals(a.get("canView"))).count()
                    : 0;

            res.put("success", true);
            res.put("message", "Saved. " + viewCount + " menu item(s) visible for this role.");
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", "Error: " + e.getMessage());
        }
        return res;
    }

    /** Clear all — role falls back to AppMenu.requiredPermission filtering. */
    @PostMapping("/{roleId}/clear")
    @ResponseBody
    @PreAuthorize("hasAuthority('PERM_ROLE_EDIT')")
    public Map<String, Object> clear(@PathVariable Long roleId) {
        Map<String, Object> res = new HashMap<>();
        try {
            rmaService.clearMenuAccess(roleId);
            res.put("success", true);
            res.put("message", "Cleared. Role now uses permission-based menu filtering.");
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", "Error: " + e.getMessage());
        }
        return res;
    }
}
