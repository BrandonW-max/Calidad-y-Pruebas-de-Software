package com.rrhh.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;

@Component
public class JwtConfig {

    private final Key secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    private final long expirationTime = 1000 * 60 * 15;

    public String generarToken(
            String username,
            String rol,
            Long idUsuario,
            List<String> permisos
    ) {

        return Jwts.builder()
                .setSubject(username)
                .claim("rol", rol)
                .claim("idUsuario", idUsuario)
                .claim("permisos", permisos)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(secretKey)
                .compact();
    }

    public Claims obtenerClaims(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String obtenerUsername(String token) {
        return obtenerClaims(token).getSubject();
    }

    public String obtenerRol(String token) {
        return obtenerClaims(token).get("rol", String.class);
    }

    public Long obtenerIdUsuario(String token) {
        Number id = obtenerClaims(token).get("idUsuario", Number.class);

        if (id == null) {
            return null;
        }

        return id.longValue();
    }

    public boolean tokenValido(String token) {
        try {
            Claims claims = obtenerClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public List<String> obtenerPermisos(String token) {
        return obtenerClaims(token).get("permisos", List.class);
    }

    public long tiempoRestanteMs(String token) {
        try {
            Claims claims = obtenerClaims(token);
            return claims.getExpiration().getTime() - System.currentTimeMillis();
        } catch (Exception e) {
            return 0;
        }
    }

    public String refrescarToken(String token) {
        Claims claims = obtenerClaims(token);
        String username = claims.getSubject();
        String rol = claims.get("rol", String.class);
        Number id = claims.get("idUsuario", Number.class);
        List<String> permisos = obtenerPermisos(token);

        Long idUsuario = id != null ? id.longValue() : null;

        return generarToken(username, rol, idUsuario, permisos);
    }
}