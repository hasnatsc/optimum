package com.hasnat.optimum.security.service;

import java.util.List;
import java.util.Map;

public interface RoleMenuAccessService {

    /**
     * Full menu tree for this role, each node annotated with current
     * canView / canCreate / canEdit / canDelete flags.
     */
    List<Map<String, Object>> getMenuTreeForRole(Long roleId);

    /**
     * Replace the complete access set for a role.
     * Each item: { menuId, canView, canCreate, canEdit, canDelete }
     */
    void saveMenuAccess(Long roleId, List<Map<String, Object>> menuAccess);

    /** Remove all entries — role reverts to AppMenu.requiredPermission filtering. */
    void clearMenuAccess(Long roleId);
}
