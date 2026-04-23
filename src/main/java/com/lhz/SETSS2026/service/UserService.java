package com.LHZ.SETSS2026.service;

import com.LHZ.SETSS2026.RoleRepository.RoleRepository;
import com.LHZ.SETSS2026.RoleRepository.UserRepository;
import com.LHZ.SETSS2026.dto.AuthResponse;
import com.LHZ.SETSS2026.entity.Role;
import com.LHZ.SETSS2026.entity.User;
import com.LHZ.SETSS2026.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleRepository roleRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.jwtUtil = jwtUtil;
    }


    // 用户注册业务
    @Transactional
    public String register(User user) {
        Optional<User> existingUser = userRepository.findByName(user.getName()).stream().findFirst();
        if (existingUser.isPresent()) {
            return "用户名已存在";
        }

        if (user.getEmail() != null && userRepository.findByEmail(user.getEmail()).isPresent()) {
            return "邮箱已被注册";
        }

        if (user.getPhone() != null && userRepository.findByPhone(user.getPhone()).isPresent()) {
            return "手机号已被注册";
        }

        // 创建用户
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnable(true);

        // 查找角色，这里注意检查数据库中的角色名是否包含隐藏的"\n"符号，避免查询失败
        Role role = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("角色不存在"));
        user.setRole(role);

        userRepository.save(user);
        return "注册成功";
    }

    // 基于用户名、密码登录，若成功返回带JWT令牌的登录响应
    public AuthResponse login(String username, String password) {
        User user = userRepository.findByName(username)
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        if (!user.getEnable()) {
            throw new RuntimeException("账户已被禁用");
        }

        String token = jwtUtil.generateToken(String.valueOf(user.getId()), user.getName());
        return new AuthResponse(token);
    }


}

