// ═════════════════════════════════════════════════════════════════════════════
// FILE 1 of 3:  UserDTO.java
// ═════════════════════════════════════════════════════════════════════════════
package com.hasnat.optimum.security.dto;

import com.hasnat.optimum.security.entity.User;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserDTO {

    private Long   id;
    private String username;
    private String fullName;
    private String email;
    private String phone;

    /** Provided only on create; blank = keep existing on update. */
    private String password;

    private boolean enabled;
    private boolean accountNonExpired;
    private boolean accountNonLocked;
    private boolean credentialsNonExpired;

    private User.DefaultDashboard defaultDashboard;

    /** IDs of roles to assign (from the multi-select in the form). */
    private List<Long> roleIds;

    // ── Display-only fields (populated by the service for view modal) ─────────
    private String roleNames;       // comma-separated role display names
    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;
}
