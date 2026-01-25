package com.example.eduaceai.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import java.util.Date;
import javax.crypto.SecretKey;

@Component
public class JwtUtils {

    private final String SECRET_KEY_STR = "EduAceAI_Secret_Key_For_Hackathon_2026_Java21";
    private final SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY_STR.getBytes());

    private final long expiration = 86400000;

    public String generateToken(String studentCode) {
        return Jwts.builder()
                .setSubject(studentCode)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractStudentCode(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}