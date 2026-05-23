package com.hasnat.optimum.security.handler;

import jakarta.servlet.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest req,
                                        HttpServletResponse res,
                                        AuthenticationException ex) throws IOException {
        String error;
        if (ex instanceof BadCredentialsException) {
            error = "invalid_credentials";
        } else if (ex instanceof LockedException) {
            error = "account_locked";
        } else if (ex instanceof DisabledException) {
            error = "account_disabled";
        } else if (ex instanceof CredentialsExpiredException) {
            error = "credentials_expired";
        } else {
            error = "unknown";
        }
        res.sendRedirect(req.getContextPath() + "/login?error=" + error);
    }
}