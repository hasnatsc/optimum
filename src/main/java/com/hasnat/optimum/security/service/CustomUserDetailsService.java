package com.hasnat.optimum.security.service;

import com.hasnat.optimum.security.entity.User;
import com.hasnat.optimum.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Supports login by username OR email OR phone.
     * Spring Security calls this with whatever the user typed in the
     * "username" field of the login form.
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String principal) throws UsernameNotFoundException {
        User user = userRepository
                .findByUsernameOrEmailOrPhone(principal, principal, principal)
                .orElseThrow(() ->
                        new UsernameNotFoundException("No user found for: " + principal));
        return new CustomUserDetails(user);
    }
}