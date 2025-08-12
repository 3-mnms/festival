package com.teckit.festival.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class HeaderAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {

        // Actuator는 건너뜀 (전역 예외/보안 간섭 방지)
        String uri = request.getRequestURI();
        if (uri != null && uri.startsWith("/actuator")) {
            chain.doFilter(request, response);
            return;
        }

        // 게이트웨이가 붙여주는 헤더
        final String loginId = trimToNull(request.getHeader("X-Login-Id"));
        final String rolesHdr = trimToNull(request.getHeader("X-User-Role")); // 예: "HOST", "ADMIN" 또는 "HOST,ADMIN"

        Authentication current = SecurityContextHolder.getContext().getAuthentication();
        boolean isAnonymous = (current instanceof AnonymousAuthenticationToken);
        boolean canSetAuth = (current == null) || isAnonymous;

        if (canSetAuth && loginId != null && rolesHdr != null) {
            List<GrantedAuthority> authorities = Arrays.stream(rolesHdr.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(String::toUpperCase)
                    .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r) // ROLE_ 중복 방지
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            System.out.println("[HeaderAuth] loginId=" + loginId +
                    ", rolesHeader=" + rolesHdr +
                    " -> authorities=" + authorities);

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(loginId, null, authorities);
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        chain.doFilter(request, response);
    }

    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
