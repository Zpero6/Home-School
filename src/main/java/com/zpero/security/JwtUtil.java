package com.zpero.security;

import com.zpero.mapper.SysUserMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secretKey;

    @Value ("${jwt.expiration-time}")
    private Long expirationTime;

    private final SysUserMapper sysUserMapper;
    private SecretKey getSecretKey(){
        return Keys.hmacShaKeyFor(
                secretKey.getBytes(StandardCharsets.UTF_8)
        );
    }

    public String generateToken(Long userId, String username, String roleCode, String tokenId){
        SecretKey secretKey = getSecretKey();

        Map<String, Object> claims = new HashMap<>();
        claims.put("id", userId);
        claims.put("username", username);
        claims.put("roleCode", roleCode);
        claims.put("tokenId", tokenId);
        return Jwts.builder()
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(secretKey)
                .compact();

    }

    public Claims parseToken(String token){
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // jwt 解析库在反序列化数字时, id 比较小会变成Integer ,比较大会变成Long ,BigDecimal
    public Long getUserId(String token){
        Object userId = parseToken(token).get("id");
        if(userId instanceof Integer i){
            return i.longValue();
        }
        return ((Number) userId).longValue();

    }

    public String getUsername(String token){
        return parseToken(token).get("username",String.class);
    }

    public String getRoleCode(String token){
        return parseToken(token).get("roleCode", String.class);
    }
    public String getTokenId(String token){
        return parseToken(token).get("tokenId", String.class);
    }
    public boolean isExpired(String token){
        Date expiration = parseToken(token).getExpiration();
        return expiration.before(new Date());
    }
    public boolean validateToken(String token){
        try{
            Claims claims = parseToken(token);
            return claims.getExpiration().after(new Date());
        }
        catch (Exception e){
            return false;
        }
    }




}
