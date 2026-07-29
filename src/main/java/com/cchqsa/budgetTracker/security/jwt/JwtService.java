package com.cchqsa.budgetTracker.security.jwt;


import com.cchqsa.budgetTracker.dto.JwtAuthenticationDto;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.validation.constraints.NotBlank;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long expiration;

    private static final Logger LOGGER = LogManager.getLogger(JwtService.class);


    public boolean validateJwtToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            LOGGER.error("ExpiredJwtException: Token is expired", e);
        } catch (UnsupportedJwtException e) {
            LOGGER.error("UnsupportedJwtException: Token format is not supported", e);
        } catch (MalformedJwtException e) {
            LOGGER.error("MalformedJwtException: Invalid token structure", e);
        } catch (io.jsonwebtoken.security.SecurityException e) {
            LOGGER.error("SecurityException: Invalid signature mapping", e);
        } catch (Exception e) {
            LOGGER.error("Invalid token error", e);
        }
        return false;
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String getUserNameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSignInKey())
                .build().parseClaimsJws(token).getPayload();
        return claims.getSubject();
    }

    public JwtAuthenticationDto generateJwtAuthToken(String username) {
        JwtAuthenticationDto authenticationDto = new JwtAuthenticationDto();
        authenticationDto.setJwtToken(generateJwtToken(username));
        authenticationDto.setRefreshToken(generateJwtRefreshToken(username));
        return authenticationDto;
    }

    private String generateJwtToken(String email) {
        Date date = Date.from(LocalDateTime.now().plusHours(1).atZone(ZoneId.systemDefault()).toInstant());
        return Jwts.builder()
                .subject(email)
                .expiration(date)
                .signWith(getSignInKey(), Jwts.SIG.HS256)
                .compact();
    }


    private String generateJwtRefreshToken(String username) {
        Date date = Date.from(LocalDateTime.now().plusDays(1).atZone(ZoneId.systemDefault()).toInstant());
        return Jwts.builder()
                .subject(username)
                .expiration(date)
                .signWith(getSignInKey(), Jwts.SIG.HS256)
                .compact();
    }


    public JwtAuthenticationDto refreshBaseToken(String username, String refreshToken) {
        JwtAuthenticationDto authenticationDto = new JwtAuthenticationDto();
        authenticationDto.setJwtToken(generateJwtToken(username));
        authenticationDto.setRefreshToken(refreshToken);
        return authenticationDto;
    }

}
