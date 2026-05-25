package com.hasnat.optimum.security.controller;

import com.hasnat.optimum.security.dto.AppMenuDTO;
import com.hasnat.optimum.security.service.AppMenuService;
import com.hasnat.optimum.utility.DataTableResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Admin CRUD for navigation menu management.
 *
 * Endpoints:
 *   GET    /admin/menus                → page
 *   GET    /admin/menus/list           → DataTable (server-side)
 *   POST   /admin/menus/save           → create / update
 *   GET    /admin/menus/show/{id}      → fetch for view/edit modal
 *   DELETE /admin/menus/delete/{id}    → soft delete (cascades to children)
 *   POST   /admin/menus/toggle/{id}    → toggle active / inactive
 *   GET    /admin/menus/parents/search → Select2 AJAX for parent picker
 */
@Controller
@RequestMapping("/admin/menus")
@RequiredArgsConstructor
public class AppMenuController {

    private final AppMenuService menuService;

    private static final DateTimeFormatter DT_FMT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    // ── Page ──────────────────────────────────────────────────────────────

    @GetMapping
    @PreAuthorize("hasAuthority('PERM_MENU_MANAGE')")
    public String index() {
        return "security/menu-management";
    }

    // ── DataTable ─────────────────────────────────────────────────────────

    @GetMapping("/list")
    @ResponseBody
    @PreAuthorize("hasAuthority('PERM_MENU_MANAGE')")
    public DataTableResponse<Map<String, Object>> list(
            @RequestParam(defaultValue = "1")  int    draw,
            @RequestParam(defaultValue = "0")  int    start,
            @RequestParam(defaultValue = "10") int    length,
            @RequestParam(value = "search[value]", defaultValue = "") String searchValue,
            @RequestParam(required = false)    String typeFilter,
            @RequestParam(required = false)    String statusFilter) {

        return menuService.datatableList(draw, start, length, searchValue, typeFilter, statusFilter);
    }

    // ── Save (create / update) ────────────────────────────────────────────

    @PostMapping("/save")
    @ResponseBody
    @PreAuthorize("hasAuthority('PERM_MENU_MANAGE')")
    public Map<String, Object> save(@RequestBody AppMenuDTO dto) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (dto.getId() != null) {
                menuService.updateMenu(dto.getId(), dto);
                response.put("success", true);
                response.put("message", "Menu updated successfully.");
            } else {
                menuService.createMenu(dto);
                response.put("success", true);
                response.put("message", "Menu created successfully.");
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

    // ── Show ──────────────────────────────────────────────────────────────

    @GetMapping("/show/{id}")
    @ResponseBody
    @PreAuthorize("hasAuthority('PERM_MENU_MANAGE')")
    public Map<String, Object> show(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            AppMenuDTO dto = menuService.getMenuById(id);
            response.put("obj",     Map.of("defaultData", toMap(dto)));
            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }
        return response;
    }

    // ── Delete ────────────────────────────────────────────────────────────

    @DeleteMapping("/delete/{id}")
    @ResponseBody
    @PreAuthorize("hasAuthority('PERM_MENU_MANAGE')")
    public Map<String, Object> delete(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            menuService.deleteMenu(id);
            response.put("success", true);
            response.put("message", "Menu deleted. Children were also removed.");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }
        return response;
    }

    // ── Toggle status ─────────────────────────────────────────────────────

    @PostMapping("/toggle/{id}")
    @ResponseBody
    @PreAuthorize("hasAuthority('PERM_MENU_MANAGE')")
    public Map<String, Object> toggle(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            menuService.toggleStatus(id);
            response.put("success", true);
            response.put("message", "Status updated.");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }
        return response;
    }

    // ── Parent select2 AJAX ───────────────────────────────────────────────

    @GetMapping("/parents/search")
    @ResponseBody
    @PreAuthorize("hasAuthority('PERM_MENU_MANAGE')")
    public List<Map<String, Object>> parentsSearch(
            @RequestParam(required = false) String search) {
        return menuService.getParentMenusForSelect(search);
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private Map<String, Object> toMap(AppMenuDTO dto) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",                 dto.getId());
        m.put("menuName",           dto.getMenuName());
        m.put("icon",               dto.getIcon() != null   ? dto.getIcon()   : "");
        m.put("menuUrl",            dto.getMenuUrl() != null ? dto.getMenuUrl() : "");
        m.put("target",             dto.getTarget() != null  ? dto.getTarget()  : "_self");
        m.put("parentId",           dto.getParentId());
        m.put("menuType",           dto.getMenuType() != null ? dto.getMenuType().name() : "LEAF");
        m.put("displayOrder",       dto.getDisplayOrder() != null ? dto.getDisplayOrder() : 0);
        m.put("requiredPermission", dto.getRequiredPermission() != null ? dto.getRequiredPermission() : "");
        m.put("active",             dto.isActive());
        m.put("createdBy",          dto.getCreatedBy() != null ? dto.getCreatedBy() : "");
        m.put("updatedBy",          dto.getUpdatedBy() != null ? dto.getUpdatedBy() : "");
        m.put("createdAt",          dto.getCreatedAt() != null ? dto.getCreatedAt().format(DT_FMT) : "");
        m.put("updatedAt",          dto.getUpdatedAt() != null ? dto.getUpdatedAt().format(DT_FMT) : "");
        return m;
    }
}
