package com.uijae.cms.domain.config;

import com.uijae.cms.domain.common.UserType;
import com.uijae.cms.domain.common.UserVo;
import com.uijae.cms.util.Aes256Util;
import java.nio.charset.StandardCharsets;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;
import java.util.Objects;

public class JwtAuthenticationProvider {

    private static final String SECRET = "YourSuperSecretKeyForJWTsYourSuperSecretKey";
    private static final Key secretKey = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    private static final long tokenValidTime = 1000L * 60 * 60 * 24; // 24 hours

    public String createToken(String userPk, Long id, UserType userType) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + tokenValidTime);

        System.out.println("Now: " + now);
        System.out.println("Expiration: " + expiration);

        String encryptedUserPk = Aes256Util.encrypt(userPk);
        String encryptedId = Aes256Util.encrypt(id.toString());

        Claims claims = Jwts.claims()
                .setSubject(encryptedUserPk)
                .setId(encryptedId);
        claims.put("roles", userType.name());

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    public boolean validateToken(String jwtToken) {
        try {
            Jws<Claims> claimsJws = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(jwtToken);

            System.out.println("Token expiration: " + claimsJws.getBody().getExpiration());
            return !claimsJws.getBody().getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            System.out.println("Token validation failed: " + e.getMessage());
            return false;
        }
    }

    public UserVo getUserVo(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        String decryptedUserPk = Aes256Util.decrypt(claims.getSubject());
        String decryptedId = Aes256Util.decrypt(claims.getId());
        Long userId = Long.valueOf(Objects.requireNonNull(decryptedId));

        return new UserVo(userId, decryptedUserPk);
    }
}
