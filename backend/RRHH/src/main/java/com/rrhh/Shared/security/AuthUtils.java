package com.rrhh.Shared.security;

import com.rrhh.config.security.AuthenticatedUser;
import org.springframework.security.core.Authentication;

public final class AuthUtils {

    private AuthUtils() {}

    public static Long extraerIdUsuario(Authentication authentication) {
        if (authentication == null) return null;
        if (authentication.getPrincipal() instanceof AuthenticatedUser authUser) {
            return authUser.getIdUsuario();
        }
        try {
            return Long.parseLong(authentication.getName());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
