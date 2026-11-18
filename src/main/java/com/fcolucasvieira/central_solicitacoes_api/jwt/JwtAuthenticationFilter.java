package com.fcolucasvieira.central_solicitacoes_api.jwt;

import com.fcolucasvieira.central_solicitacoes_api.user.entity.User;
import com.fcolucasvieira.central_solicitacoes_api.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            String subject = jwtService.extractClaims(token).getSubject();

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                User user = userRepository.findByEmail(subject)
                        .orElse(null);

                if (user != null && jwtService.isTokenValid(token, user)) {
                    var authToken = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception ex) {
            log.warn("Token JWT inválido ou expirado: {}", ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
