package org.example.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.example.user.UserEntity;
import org.example.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JwtUtil jwtUtil;
    private final UserService userService;

    public JwtAuthFilter(JwtUtil jwtUtil, UserService userService) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestUri = request.getRequestURI();
        String auth = request.getHeader("Authorization");

        if (!StringUtils.hasText(auth)) {
            log.debug("No Authorization header found for request: {}", requestUri);
        } else if (!auth.startsWith("Bearer ")) {
            log.debug("Authorization header does not start with 'Bearer ' for request: {}", requestUri);
        } else {
            String token = auth.substring(7);
            log.debug("Processing JWT token for request: {}", requestUri);

            try {
                Jws<Claims> jws = jwtUtil.parse(token);
                Claims claims = jws.getBody();
                String username = claims.getSubject();
                Long userId = claims.get("uid", Long.class);
                Integer tv = claims.get("tv", Integer.class);

                if (username == null || userId == null) {
                    log.warn("Token missing username or userId for request: {}", requestUri);
                } else {
                    UserEntity user = userService.getById(userId);
                    if (user == null) {
                        log.warn("User not found for userId: {} in request: {}", userId, requestUri);
                    } else if (user.getDeleted() != 0) {
                        log.warn("User {} is deleted for request: {}", userId, requestUri);
                    } else if (user.getStatus() != 1) {
                        log.warn("User {} status is {} (not active) for request: {}", userId, user.getStatus(),
                                requestUri);
                    } else {
                        int currentTv = user.getTokenVersion() == null ? 0 : user.getTokenVersion();
                        int tokenTv = tv == null ? 0 : tv;
                        if (currentTv != tokenTv) {
                            log.warn("Token version mismatch for user {}: current={}, token={} for request: {}",
                                    userId, currentTv, tokenTv, requestUri);
                        } else if (SecurityContextHolder.getContext().getAuthentication() == null) {
                            List<String> authCodes = userService.getAuthorities(userId);
                            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                            for (String a : authCodes) {
                                authorities.add(new SimpleGrantedAuthority(a));
                            }
                            UserPrincipal principal = new UserPrincipal(userId, username);
                            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                                    principal, null, authorities);
                            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                            log.debug("Successfully authenticated user {} with authorities {} for request: {}",
                                    username, authCodes, requestUri);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("JWT token validation failed for request: {} - Error: {}", requestUri, e.getMessage());
            }
        }
        filterChain.doFilter(request, response);
    }
}
