// JwtFilter.java 修复核心逻辑错误
package com.LHZ.SETSS2026.security;


import com.LHZ.SETSS2026.service.JwtAuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtAuthService jwtAuthService;


    private boolean isPublicApi(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        if (WHITE_LIST.contains(path)) {
            return true;
        }

        if ("GET".equalsIgnoreCase(method) && path.equals("/api/posts")) {
            return true;
        }
        if ("GET".equalsIgnoreCase(method) && path.matches("^/api/posts/\\d+$")) {
            return true;
        }

        if ("GET".equalsIgnoreCase(method) && path.matches("^/api/posts/\\d+/comments$")) {
            return true;
        }

        if ("GET".equalsIgnoreCase(method) && path.startsWith("/api/pages/")) {
            System.out.println("请求pages：" + path);
            return true;
        }


        return false;
    }
    // 白名单路径统一管理
    private static final List<String> WHITE_LIST = Arrays.asList(
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/validate"
    );

    private boolean isStaticResource(String path) {
        return path.startsWith("/static/") ||
                path.startsWith("/public/") ||
                path.matches(".*\\.(html|css|js|png|jpg|jpeg|gif|ico|svg)$");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        System.out.println("请求路径：" + path);
        // 关键：OPTIONS 预检请求直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        if (isPublicApi(request) ||
                isStaticResource(path) ||
                path.startsWith("/chat/")) {
            filterChain.doFilter(request, response);
            return;
        }
        System.out.println("请求路径：" + path);
        // 提取token
        String token = extractTokenFromHeader(request);
        if (token == null) {
            token = extractTokenFromQuery(request);
        }
        System.out.println("token：" + token);
        // 验证token并设置认证信息 - 修复逻辑判断错误
        if (token != null && !jwtAuthService.isTokenInvalid(token)) {
            try {
                CustomUserDetails userDetails = jwtAuthService.loadUserByToken(token);
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception e) {
                System.out.println("JWT解析失败: " + e.getMessage());
                SecurityContextHolder.clearContext(); // 出错时清除上下文
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT解析失败: " + e.getMessage());
                return;
            }
        } else {
            System.out.println("认证信息无效");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "无效的认证信息");
            return;
        }
        System.out.println("认证信息设置成功");
        filterChain.doFilter(request, response);
    }

    private String extractTokenFromHeader(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7).trim(); // 增加trim处理防止空格问题
        }
        return null;
    }

    private String extractTokenFromQuery(HttpServletRequest request) {
        String token = request.getParameter("token");
        if (token != null) {
            try {
                return java.net.URLDecoder.decode(token, "UTF-8");
            } catch (Exception e) {
                return token;
            }
        }
        return null;
    }
}