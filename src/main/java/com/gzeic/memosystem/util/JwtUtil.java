package com.gzeic.memosystem.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT工具类
 *
 * 功能：
 * - 生成JWT令牌
 * - 解析JWT令牌
 * - 验证JWT令牌
 *
 * @author jml
 */
@Component
public class JwtUtil {

    /**
     * JWT密钥
     */
    @Value("${app.jwt.secret}")
    private String secret;

    /**
     * 过期时间（毫秒）
     */
    @Value("${app.jwt.expiration}")
    private Long expiration;

    /**
     * 获取签名密钥
     *
     * @return HMAC-SHA签名密钥
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成JWT令牌
     *
     * @param username 用户名
     * @param userId   用户ID
     * @param tenantId 租户ID
     * @return JWT令牌字符串
     */
    public String generateToken(String username, Long userId, Long tenantId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("tenantId", tenantId)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 生成JWT令牌（不包含租户ID）
     *
     * @param username 用户名
     * @param userId   用户ID
     * @return JWT令牌字符串
     */
    public String generateToken(String username, Long userId) {
        return generateToken(username, userId, null);
    }

    /**
     * 从令牌中获取用户名
     *
     * @param token JWT令牌
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    /**
     * 从令牌中获取用户ID
     *
     * @param token JWT令牌
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.get("userId", Long.class);
    }

    /**
     * 从令牌中获取租户ID
     *
     * 说明：
     * - tenantId 是多租户隔离的关键字段
     * - 后续会写入 TenantContext，供 MyBatis-Plus 租户插件使用
     *
     * @param token JWT令牌
     * @return 租户ID
     */
    public Long getTenantIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.get("tenantId", Long.class);
    }

    /**
     * 验证令牌是否有效
     *
     * @param token JWT令牌
     * @return true=有效，false=无效
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            // 不记录具体错误信息，防止敏感信息泄露
            return false;
        }
    }

    /**
     * 获取令牌的过期时间
     *
     * @param token JWT令牌
     * @return 过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload().getExpiration();
    }
}