package com.hasnat.optimum.security.service;

/**
 * Contract for the password-reset flow:
 *  1. initiateReset(email)       → creates token, sends email (or logs in dev)
 *  2. validateToken(token)       → checks token is still valid, returns result
 *  3. resetPassword(token, pass) → updates password and marks token used
 */
public interface PasswordResetService {

    /** Returns true if an email was found and a reset link was dispatched. */
    boolean initiateReset(String email);

    /** Returns true if the token exists, is unused, and has not expired. */
    boolean isTokenValid(String token);

    /**
     * Applies the new password.
     * @throws IllegalArgumentException if the token is invalid/expired/used
     */
    void resetPassword(String token, String newPassword);
}
