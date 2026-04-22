package com.lhz.SETSS2026.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {
    // 建议通过配置文件读取密钥，并确保长度满足HS256要求（≥ 32 字节）
    private final SecretKey key = Keys.hmacShaKeyFor("mySecretKey123456789012345678901234".getBytes());

    public String extractId(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * 生成JWT令牌，设置主题、签发时间、过期时间、密钥
     */
    public String generateToken(String id, String name) {
        long expiration = 3600000; // 1 hour
        return Jwts.builder()
                .subject(id) // 主题 - typically user ID
                .claim("name", name) // 添加 name 作为自定义声明
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
    }

    // 提取token中的主题，一般是用户名
    public String extractUserName(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("name", String.class); // Extract custom claim "name"
    }

    // 验证令牌是否有效（签名正确、未被篡改）
    public boolean validateToken(String token) {
        try {
            Jwts.parser() // 创建解析器构建器
                    .verifyWith(key) // 设置签名验证使用的密钥
                    .build() // 构建解析器实例
                    .parseSignedClaims(token);  // 若验证失败，这里会抛异常
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        // 判断 Authorization 头是否存在且格式为 Bearer 开头
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7); // 去掉 "Bearer " 前缀
        }
        return null; // 没有token或格式不对
    }
}
