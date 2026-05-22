package com.uees.studentservices.security;

import com.uees.studentservices.exception.DomainException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class AuthUtils {

    private AuthUtils() {}

    public static StudentPrincipal currentPrincipal() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || a.getPrincipal() == null || !(a.getPrincipal() instanceof StudentPrincipal sp)) {
            throw DomainException.forbidden("Sesion no autenticada");
        }
        return sp;
    }
}
