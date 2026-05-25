package com.hasnat.optimum.security.service;

import com.hasnat.optimum.security.entity.Role;
import com.hasnat.optimum.security.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
public class CustomUserDetails implements UserDetails {

    private final User                  user;
    private final Set<GrantedAuthority> authorities;

    public CustomUserDetails(User user) {
        this.user        = user;
        this.authorities = buildAuthorities(user);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Authority builder
    // ─────────────────────────────────────────────────────────────────────────

    private Set<GrantedAuthority> buildAuthorities(User user) {
        Set<GrantedAuthority> auths = new LinkedHashSet<>();

        for (Role role : user.getRoles()) {

            // FIX: getMasterRole() returns a MasterRole enum, not a String.
            // SimpleGrantedAuthority requires a String — call .name() to convert.
            // e.g. MasterRole.ROLE_SUPER_ADMIN  →  "ROLE_SUPER_ADMIN"
            if (role.getMasterRole() != null) {
                auths.add(new SimpleGrantedAuthority(role.getMasterRole().name()));
            }

            // Add every fine-grained permission assigned to the role.
            // e.g. "PERM_PRODUCTION_ORDER_CREATE", "PERM_USER_VIEW" …
            if (role.getPermissions() != null) {
                role.getPermissions().forEach(p -> {
                    if (p.getName() != null && !p.getName().isBlank()) {
                        auths.add(new SimpleGrantedAuthority(p.getName()));
                    }
                });
            }
        }

        return auths;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UserDetails contract
    // ─────────────────────────────────────────────────────────────────────────

    @Override public String getPassword() { return user.getPassword(); }
    @Override public String getUsername() { return user.getUsername(); }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }

    @Override public boolean isEnabled()               { return user.isEnabled(); }
    @Override public boolean isAccountNonExpired()     { return user.isAccountNonExpired(); }
    @Override public boolean isAccountNonLocked()      { return user.isAccountNonLocked(); }
    @Override public boolean isCredentialsNonExpired() { return user.isCredentialsNonExpired(); }

    // ─────────────────────────────────────────────────────────────────────────
    // Convenience helpers — used in Thymeleaf / controllers
    // ─────────────────────────────────────────────────────────────────────────

    public Long   getUserId()          { return user.getId(); }
    public String getFullName()        { return user.getFullName(); }
    public String getEmail()           { return user.getEmail(); }
    public User.DefaultDashboard getDefaultDashboard() { return user.getDefaultDashboard(); }

    /**
     * Quick role check — avoids iterating authorities on every call.
     * Usage: principal.hasRole("ROLE_SUPER_ADMIN")
     */
    public boolean hasRole(String roleName) {
        return authorities.stream()
            .anyMatch(a -> a.getAuthority().equals(roleName));
    }

    /**
     * Quick permission check.
     * Usage: principal.hasPermission("PERM_USER_CREATE")
     */
    public boolean hasPermission(String permName) {
        return authorities.stream()
            .anyMatch(a -> a.getAuthority().equals(permName));
    }
}
