package com.lhz.SETSS2026.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lhz.SETSS2026.RoleReporory.RoleRepository;
import com.lhz.SETSS2026.RoleReporory.UserRepository;
import com.lhz.SETSS2026.entity.User;
import com.lhz.SETSS2026.security.CustomUserDetails;
import com.lhz.SETSS2026.security.JwtUtil;
import org.apache.catalina.Role;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class JwtAuthService {

    final private JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private static final String prefix = "user:roles:"; // key的前缀
    final private StringRedisTemplate stringRedisTemplate; // 缓存操作模板
    final private ObjectMapper objectMapper; // JSON转换器
    public JwtAuthService(JwtUtil jwtUtil, UserRepository userRepository, RoleRepository roleRepository, StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    // 获取用户权限
    // 获取用户权限
    private List<GrantedAuthority> getAuthoritiesByUserName(String username) {
        User user = userRepository.findByName(username)
                .stream()
                .findFirst()
                .orElseThrow(() -> new UsernameNotFoundException("不存在用户：" + username));

        List<String> roles = user.getRole() != null
                ? Collections.singletonList(user.getRole().getName())
                : Collections.emptyList();

        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }


    // 基于JWT鉴权并封装用户详情
    public CustomUserDetails loadUserByToken(String token) {
        // 检查token是否无效或否已注销
        if (isTokenInvalid(token)) {
            throw new RuntimeException("令牌无效或已注销");
        }
        String username = jwtUtil.extractUserName(token);
        // 尝试从缓存获取权限
        List<String> roles = getCachedRoles(username);
        if (!roles.isEmpty()) {
            // 用户权限存在缓存，说明用户存在，避免DB查询
            List<GrantedAuthority> authorities = roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
            return new CustomUserDetails(0, username, "", authorities); // userId 可为空或另行缓存
        }
        // 缓存缺失：访问数据库确认用户存在性，并尝试获取权限
        User user = userRepository.findByName(username)
                .stream()
                .findFirst()
                .orElseThrow(() -> new UsernameNotFoundException("不存在用户：" + username));
        List<String> fetchedRoles = user.getRole() != null
                ? Collections.singletonList(user.getRole().getName())
                : Collections.emptyList();
        // 写入缓存
        cacheRoles(username, fetchedRoles);
        List<GrantedAuthority> authorities = fetchedRoles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        return new CustomUserDetails(user.getId(), username, "", authorities);
    }


    // 清除令牌
    public void invalidateToken(String token) {
        stringRedisTemplate.opsForValue().set("blacklist:" + token, "1", 10, TimeUnit.MINUTES);
    }


    // 判断token是否无效
    public boolean isTokenInvalid(String token) {
        return !jwtUtil.validateToken(token) || stringRedisTemplate.hasKey("blacklist:" + token); // 增加黑名单判断
    }

    private void cacheRoles(String username, List<String> roles)  {
        String json = null;
        try {
            json = objectMapper.writeValueAsString(roles); // 转换为JSON字符串
        } catch (JsonProcessingException e) {
            throw new RuntimeException("将用户权限列表序列化为JSON失败，username=" + username + ", roles=" + roles, e);
        }
        stringRedisTemplate.opsForValue().set(prefix + username, json, 1, TimeUnit.HOURS);
    }

    private List<String> getCachedRoles(String username) {
        String json = stringRedisTemplate.opsForValue().get(prefix + username);
        if (json == null) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("反序列化Redis中"+prefix + username + "的缓存失败，内容为：" + json, e);
        }
    }
}
