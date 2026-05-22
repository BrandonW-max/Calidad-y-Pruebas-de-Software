package com.rrhh.AutenticacionYRol.Infrastructure.config;

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

    public String generarToken(String username, String rol) {

        return Jwts.builder()
                .setSubject(username)
                .claim("rol", rol)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(secretKey)
                .compact();
    }
}