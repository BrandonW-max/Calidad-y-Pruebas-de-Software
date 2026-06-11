package com.rrhh.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtConfig jwtConfig;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        if (!jwtConfig.tokenValido(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        String username = jwtConfig.obtenerUsername(token);
        String rol = jwtConfig.obtenerRol(token);
        Long idUsuario = jwtConfig.obtenerIdUsuario(token);

        LocalDateTime tokenInvalidoDesde = jwtConfig.obtenerTokenInvalidoDesde(token);
        if (tokenInvalidoDesde != null
                && jwtConfig.obtenerClaims(token).getIssuedAt()
                        .before(java.util.Date.from(
                                tokenInvalidoDesde
                                        .atZone(ZoneId.systemDefault())
                                        .toInstant()
                        ))
        ) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token invalido. El usuario ha sido desactivado.");
            return;
        }

        List<String> permisos = jwtConfig.obtenerPermisos(token);

        if (
                username != null
                && rol != null
                && idUsuario != null
                && SecurityContextHolder.getContext().getAuthentication() == null
        ) {

            AuthenticatedUser authenticatedUser =
                    new AuthenticatedUser(
                            idUsuario,
                            username,
                            rol
                    );

            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_" + rol));

            if (permisos != null) {
                for (String permiso : permisos) {
                    authorities.add(new SimpleGrantedAuthority(permiso));
                }
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            authenticatedUser,
                            null,
                            authorities
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        long tiempoRestante = jwtConfig.tiempoRestanteMs(token);
        if (tiempoRestante > 0 && tiempoRestante < 1000 * 60 * 5) {
            String nuevoToken = jwtConfig.refrescarToken(token);
            response.setHeader("X-New-Token", nuevoToken);
        }

        filterChain.doFilter(request, response);
    }
}