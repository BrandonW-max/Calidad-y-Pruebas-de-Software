package com.rrhh.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtConfig {

    private final Key secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    private final long expirationTime = 1000 * 60 * 15;

    public String generarToken(
            String username,
            String rol,
            Long idUsuario
    ) {

        return Jwts.builder()
                .setSubject(username)
                .claim("rol", rol)
                .claim("idUsuario", idUsuario)
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
}