package com.pethub.utils;

import com.pethub.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类。
 * 负责两件事：
 * 1. 登录成功后生成 token
 * 2. 后续请求时解析 token
 */
@Component
public class JwtUtil {

    private SecretKey secretKey;
    public JwtUtil(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    //构造器注入
    private final JwtProperties jwtProperties;

    @PostConstruct
    public void init() {
        // 根据配置里的密钥初始化签名 key，后续生成和解析 token 都会用到它。
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8));
    }

    public String createToken(Long adminId, String username) {
        // token 中只放当前登录管理员的核心信息，不放密码。
        Map<String, Object> claims = new HashMap<>();
        claims.put("adminId", adminId);
        claims.put("username", username);

        return Jwts.builder()
                .claims(claims)
                .subject("admin-login")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.getTtl()))
                .signWith(secretKey, resolveAlgorithm())
                .compact();
    }

    public Claims parseToken(String token) {
        // 解析成功后可以拿到 token 中保存的 claims 数据。
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private io.jsonwebtoken.security.MacAlgorithm resolveAlgorithm() {
        // 按配置选择签名算法，默认使用 HS256。
        String algorithm = jwtProperties.getAlgorithm();
        if ("HS384".equalsIgnoreCase(algorithm)) {
            return Jwts.SIG.HS384;
        }
        if ("HS512".equalsIgnoreCase(algorithm)) {
            return Jwts.SIG.HS512;
        }
        return Jwts.SIG.HS256;
    }
}
