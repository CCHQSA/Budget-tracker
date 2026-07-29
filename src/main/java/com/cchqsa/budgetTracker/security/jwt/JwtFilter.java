package com.cchqsa.budgetTracker.security.jwt;

import com.cchqsa.budgetTracker.security.CustomUserDetails;
import com.cchqsa.budgetTracker.security.CustomUserServiceImp;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserServiceImp customUserService;

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain) throws ServletException, IOException {
        String token = getTokenFromRequest(request);
        if (token != null && jwtService.validateJwtToken(token)) {
            try {
                setCustomUserDetailsToSecurityContextHolder(token);
            } catch (UsernameNotFoundException ex) {
                SecurityContextHolder.clearContext();
                clearAuthCookies(response);
            }
        }
        filterChain.doFilter(request, response);
    }

    private void clearAuthCookies(@NotNull HttpServletResponse response) {
        clearCookie(response, "jwtToken");
        clearCookie(response, "refreshToken");
    }

    private void clearCookie(@NotNull HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private void setCustomUserDetailsToSecurityContextHolder(String token) {
        String username = jwtService.getUserNameFromToken(token);
        CustomUserDetails customUserDetails =
                customUserService.loadUserByUsername(username);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        customUserDetails,
                        null,
                        customUserDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private String getTokenFromRequest(@NotNull HttpServletRequest request) {
        String bearer = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwt".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();

        return path.equals("/")
                || path.equals("/login")
                || path.equals("/register")
                || path.startsWith("/auth/")
                || path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/images/")
                || path.equals("/favicon.ico");
    }

}
