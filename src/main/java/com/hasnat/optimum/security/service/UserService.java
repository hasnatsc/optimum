package com.hasnat.optimum.security.service;

import com.hasnat.optimum.security.dto.UserDTO;
import com.hasnat.optimum.utility.DataTableResponse;

import java.util.List;
import java.util.Map;

public interface UserService {

    UserDTO createUser(UserDTO dto);

    UserDTO updateUser(Long id, UserDTO dto);

    UserDTO getUserById(Long id);

    void deleteUser(Long id);

    void toggleStatus(Long id);

    void changePassword(Long id, String newPassword);

    DataTableResponse<Map<String, Object>> datatableList(int draw, int start, int length, String searchValue, String statusFilter);

    /** Select2 AJAX — returns active roles for the role assignment dropdown. */
    List<Map<String, Object>> searchRolesForSelect2(String search);
}
