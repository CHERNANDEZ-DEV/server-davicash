package com.davivienda.factoraje.utils;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;

public class JwtUtil {

    private static final String SECRET_STRING = "estaEsUnaClaveSecretaMuchoMasLargaYSeguraParaHS256";

    private static final Key SECRET_KEY = Keys.hmacShaKeyFor(SECRET_STRING.getBytes());
    private static final long EXPIRATION_TIME = 5 * 60 * 1000;

    public static String generateToken(String DUI) {
        return Jwts.builder()
                .setSubject(DUI)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY)
                .compact();
    }

public static boolean validateToken(String token) {
    try {
        Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token);
        return true;
    } catch (ExpiredJwtException e) {
        System.out.println("Error: El token ha expirado.");
    } catch (MalformedJwtException e) {
        System.out.println("Error: El token está mal formado.");
    } catch (Exception e) {
        System.out.println("Error inesperado: " + e.getMessage());
    }
    return false;
}

    public static String getDUIFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

}
