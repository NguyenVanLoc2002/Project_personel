package com.fit.project_personel.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class JwtService {
    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.access-exp-minutes}")
    private long accessExpMin;

    @Value("${app.refresh-exp-days}")
    private long refreshExpMin;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(Base64.getEncoder().encodeToString(secret.getBytes()));
        secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(UUID userId, String email, Collection<String> roles, Collection<String> permissions) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(accessExpMin * 60);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId.toString());
        claims.put("roles", roles);
        claims.put("permissions", permissions);

        return Jwts.builder()
                .subject(email)
                .claims(claims)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    public String generateRefreshToken(UUID userId, String email) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(refreshExpMin * 60);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId.toString());

        return Jwts.builder()
                .subject(email)
                .claim("userId", userId.toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    public Jws<Claims> parseToken(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseClaimsJws(token);
    }

    public boolean isTokenExpired(String token) {
        try {
            Date expiration = parseToken(token).getPayload().getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public String getSubject(String token) {
        return parseToken(token).getPayload().getSubject();
    }

    @SuppressWarnings("unchecked")
    public Collection<String> extractListClaim(String token, String claimKey) {
        Object claim = parseToken(token).getPayload().get(claimKey);
        if (claim instanceof Collection<?> col) {
            return col.stream().map(String::valueOf).collect(Collectors.toSet());
        }
        return Collections.emptyList();
    }

    public String extract(String token, String claimKey) {
        Object claim = parseToken(token).getPayload().get(claimKey);
        return claim != null ? claim.toString() : null;
    }

}
