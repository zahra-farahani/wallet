package com.snapppay.wallet.security.jwt;

import com.snapppay.wallet.dto.enumeration.RoleType;
import com.snapppay.wallet.entity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Value("${jwt.expiration}")
    private Duration EXPIRATION_TIME;

    public String generateToken(String username, Long userId, Set<Role> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        // Convert UserRole entities to list of role names (strings)
        claims.put("roles", roles.stream()
                .map(role -> role.getType().name())
                .collect(Collectors.toList()));

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME.toMillis()))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    public Set<RoleType> extractRoles(String token) {
        return extractClaim(token,
                claims -> {
                    List<String> roleStrings = (List<String>) claims.get("roles");
                    if (roleStrings == null || roleStrings.isEmpty()) {
                        return Collections.emptySet();
                    }
                    return roleStrings.stream()
                            .map(RoleType::valueOf)
                            .collect(Collectors.toSet());
                });
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private SecretKey getSigningKey() {
        String encodedSecret = Base64.getEncoder().encodeToString(
                SECRET_KEY.getBytes(StandardCharsets.UTF_8)
        );
        byte[] keyBytes = encodedSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
