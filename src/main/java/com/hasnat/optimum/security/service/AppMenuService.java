package com.hasnat.optimum.security.service;

import com.hasnat.optimum.security.dto.AppMenuDTO;
import com.hasnat.optimum.utility.DataTableResponse;

import java.util.List;
import java.util.Map;

public interface AppMenuService {

    // ── User-facing menu (permission-filtered flat list) ──────────────────

    /**
     * Build the permission-filtered flat menu list for the current
     * authenticated user.  The JS buildMenuTree() constructs the tree.
     *
     * Algorithm:
     *  1. Load all active menus (one query).
     *  2. Mark each menu visible if:
     *       – user is SUPER_ADMIN, OR
     *       – menu has no requiredPermission, OR
     *       – user has the requiredPermission.
     *  3. For every visible menu, include its full ancestor chain so
     *     the tree renders correctly even if intermediate nodes have
     *     their own permission requirements.
     *  4. Return the filtered flat list ordered by displayOrder.
     *
     * @return  { success: true, menus: { items: [...] } }
     */
    Map<String, Object> getUserMenuResponse();

    // ── Admin CRUD ────────────────────────────────────────────────────────

    AppMenuDTO createMenu(AppMenuDTO dto);

    AppMenuDTO updateMenu(Long id, AppMenuDTO dto);

    AppMenuDTO getMenuById(Long id);

    void deleteMenu(Long id);

    void toggleStatus(Long id);

    DataTableResponse<Map<String, Object>> datatableList(
            int draw, int start, int length, String searchValue,
            String typeFilter, String statusFilter);

    /** Active MODULE + GROUP items for the parent-selector dropdown. */
    List<Map<String, Object>> getParentMenusForSelect(String search);
}
