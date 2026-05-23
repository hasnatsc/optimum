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

    private final User user;
    private final Set<GrantedAuthority> authorities;

    public CustomUserDetails(User user) {
        this.user = user;
        this.authorities = buildAuthorities(user);
    }

    private Set<GrantedAuthority> buildAuthorities(User user) {
        Set<GrantedAuthority> auths = new LinkedHashSet<>();
        for (Role role : user.getRoles()) {
            // Add role itself  e.g. ROLE_SUPER_ADMIN
            if (role.getMasterRole() != null) {
                auths.add(new SimpleGrantedAuthority(role.getMasterRole()));
            }
            // Add every permission name assigned to the role  e.g. PERM_USER_CREATE
            role.getPermissions().forEach(p ->
                    auths.add(new SimpleGrantedAuthority(p.getName()))
            );
        }
        return auths;
    }

    // ── UserDetails contract ──────────────────────────────────────────────────

    @Override public String getPassword()    { return user.getPassword(); }
    @Override public String getUsername()    { return user.getUsername(); }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }

    @Override public boolean isEnabled()                  { return user.isEnabled(); }
    @Override public boolean isAccountNonExpired()        { return user.isAccountNonExpired(); }
    @Override public boolean isAccountNonLocked()         { return user.isAccountNonLocked(); }
    @Override public boolean isCredentialsNonExpired()    { return user.isCredentialsNonExpired(); }

    // Convenience helpers used in Thymeleaf / controllers
    public Long   getUserId()          { return user.getId(); }
    public String getFullName()        { return user.getFullName(); }
    public String getEmail()           { return user.getEmail(); }
    public User.DefaultDashboard getDefaultDashboard() { return user.getDefaultDashboard(); }
}