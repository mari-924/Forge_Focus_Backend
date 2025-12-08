package com.focusforge.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    // Store a BASE64-encoded secret in env: JWT_SECRET=base64string
    @Value("${jwt.secret}")
    private String SECRET_B64;

    // 12 hours (match your current behavior)
    private static final long EXP_MILLIS = 12 * 60 * 60 * 1000L;

    private Key signingKey() {
        // Decode Base64 → bytes → strong HMAC key
        byte[] raw = Base64.getDecoder().decode(SECRET_B64);
        return Keys.hmacShaKeyFor(raw); // ensures length & type are valid for HMAC
    }

    public String generateToken(String email) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + EXP_MILLIS);

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(exp)
                // Explicit alg for clarity/strength; requires sufficiently long secret (>=64 bytes for HS512)
                .signWith(signingKey(), SignatureAlgorithm.HS512)
                .compact();
    }
    public String generateTokenWithClaims(String email, Map<String, Object> claims) {
        return Jwts.builder()
                .setSubject(email)
                .addClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(Date.from(Instant.now().plus(7, ChronoUnit.DAYS)))
                .signWith(signingKey(), SignatureAlgorithm.HS512)
                .compact();
    }
    public String extractEmail(String token) {
        return getAllClaims(token).getSubject();
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = getAllClaims(token);
            Date exp = claims.getExpiration();
            return exp != null && exp.after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    private Claims getAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}