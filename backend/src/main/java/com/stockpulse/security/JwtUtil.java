package com.stockpulse.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * JWT Utility — generates, validates, and parses JWT tokens.
 */
@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration.ms}")
    private long jwtExpirationMs;

    // ── Generate token ──────────────────────────────────────
    public String generateToken(Authentication authentication) {
        UserDetails user = (UserDetails) authentication.getPrincipal();
        return buildToken(user.getUsername());
    }

    public String generateToken(String email) {
        return buildToken(email);
    }

    private String buildToken(String subject) {
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ── Extract email from token ────────────────────────────
    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    // ── Validate token ──────────────────────────────────────
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (MalformedJwtException e)  { log.warn("Invalid JWT token"); }
        catch (ExpiredJwtException e)       { log.warn("JWT token expired"); }
        catch (UnsupportedJwtException e)   { log.warn("JWT token unsupported"); }
        catch (IllegalArgumentException e)  { log.warn("JWT claims empty"); }
        return false;
    }

    // ── Helpers ─────────────────────────────────────────────
    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }
}
