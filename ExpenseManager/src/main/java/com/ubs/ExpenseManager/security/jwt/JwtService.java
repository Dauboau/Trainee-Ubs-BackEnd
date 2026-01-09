package com.ubs.ExpenseManager.security.jwt;

import com.ubs.ExpenseManager.entities.employee.Employee;
import com.ubs.ExpenseManager.utils.Constants.TokenClaims;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class JwtService {

    @Value("${JWT_SECRET}")
    private String JWT_SECRET;

    @Value("${JWT_EXPIRATION_TIME}")
    private long JWT_EXPIRATION_TIME;

    private Key getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(JWT_SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Employee userDetails){
        Date currentDate = new Date();
        Date expirationDate = new Date(currentDate.getTime() + JWT_EXPIRATION_TIME);

        return Jwts.builder()
            .addClaims(setClaims(userDetails))
            .setIssuedAt(currentDate)
            .setExpiration(expirationDate)
            .signWith(getKey(), SignatureAlgorithm.HS256)
            .compact();
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(getAllClaims(token));
    }

    public String extractLogin(String token) {
        return extractClaim(token, claims -> claims.get(TokenClaims.LOGIN, String.class));
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        return extractLogin(token).equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Claims getAllClaims(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(getKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
    }

    private Map<String,Object> setClaims(Employee userDetails){
        Map<String, Object> userMap = new HashMap<>();
        userMap.put(TokenClaims.ID, userDetails.getId());
        userMap.put(TokenClaims.LOGIN, userDetails.getEmail());
        userMap.put(TokenClaims.ROLE, userDetails.getRole());
        return userMap;
    }
}
