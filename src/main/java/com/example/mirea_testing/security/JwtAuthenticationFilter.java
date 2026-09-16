package com.example.mirea_testing.security;

import com.example.mirea_testing.service.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        try {
            String username = jwtService.getUsername(token);
            GrantedAuthority role = jwtService.getRole(token);

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        List.of(role)
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (JwtException | IllegalArgumentException exception) {
            // Битый, подделанный или протухший токен — это не ошибка сервера.
            // Просто не аутентифицируем запрос: дальше по цепочке AuthorizationFilter
            // его отклонит, а ExceptionTranslationFilter увидит анонима и позовёт
            // AuthenticationEntryPoint, который отдаст 401 в том же формате.
            SecurityContextHolder.clearContext();
            logger.debug("Rejected JWT: " + exception.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
