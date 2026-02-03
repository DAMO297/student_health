package org.example.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {
    private SecurityUtil() {
    }

    public static String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null)
            return null;
        Object principal = auth.getPrincipal();
        if (principal instanceof UserPrincipal) {
            return ((UserPrincipal) principal).getUsername();
        }
        return principal.toString();
    }

    public static Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null)
            return null;
        Object principal = auth.getPrincipal();
        if (principal instanceof UserPrincipal) {
            return ((UserPrincipal) principal).getId();
        }
        return null; // Fallback or throw?
    }
}

