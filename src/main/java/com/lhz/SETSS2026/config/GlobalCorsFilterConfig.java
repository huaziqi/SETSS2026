package com.LHZ.SETSS2026.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class GlobalCorsFilterConfig {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE) // 最高优先级，确保先于所有过滤器执行
    public CorsFilter corsFilter() {
        // 1. 配置 CORS 规则
        CorsConfiguration config = new CorsConfiguration();
        // 允许的来源（支持通配符，兼容 allowCredentials=true）
        config.setAllowedOriginPatterns(List.of("http://*:5173"));
        config.setAllowedOriginPatterns(List.of(
                "http://localhost:5173"
        ));
        // 允许携带凭证（Cookie/Token）
        config.setAllowCredentials(true);
        // 允许所有请求方法（GET/POST/PUT/DELETE/OPTIONS）
        config.setAllowedMethods(List.of("*"));
        // 允许所有请求头（包括自定义头如 Authorization）
        config.setAllowedHeaders(List.of("*"));
        // 暴露响应头（如需前端获取自定义头）
        config.setExposedHeaders(List.of("Authorization"));
        // 预检请求有效期（1小时）
        config.setMaxAge(3600L);

        // 2. 应用到所有路径
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config); // 覆盖所有接口

        return new CorsFilter(source);

    }
}
