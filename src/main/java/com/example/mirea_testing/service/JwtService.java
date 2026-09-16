package com.example.mirea_testing.service;

import com.example.mirea_testing.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${app.jwt.secret-key}")
    private String secretKey;

    @Value("${app.jwt.expiration-time}")
    private Long expirationTime;

    public String generateToken(User user) {
        Map<String, Object> config = new HashMap<>();
        config.put("role", "ROLE_" + user.getRole());

        return createToken(user.getUsername(), config);
    }

    private String createToken(String username, Map<String, Object> claims) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + expirationTime);

        return Jwts
                .builder()
                .subject(username)
                .signWith(getSigningKey())
                .claims(claims)
                .issuedAt(now)
                .expiration(expireDate)
                .compact();
    }

    public String getUsername(String token) {
        return extractClaims(token, Claims::getSubject);
    }

    public GrantedAuthority getRole(String token) {
        return new SimpleGrantedAuthority(extractClaims(token, c -> c.get("role", String.class)));
    }

    private <T> T extractClaims(String token, Function<Claims, T> extractor) {
        return extractor.apply(getClaims(token));
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

    }

    private SecretKey getSigningKey() {
        byte[] bytes = secretKey.getBytes(StandardCharsets.UTF_16);
        return Keys.hmacShaKeyFor(bytes);
    }
}
