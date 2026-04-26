package com.LHZ.SETSS2026.service;

import com.LHZ.SETSS2026.dto.RegisterRequest;
import com.LHZ.SETSS2026.dto.UserInfoResponse;
import com.LHZ.SETSS2026.repository.RoleRepository;
import com.LHZ.SETSS2026.repository.UserRepository;
import com.LHZ.SETSS2026.dto.AuthResponse;
import com.LHZ.SETSS2026.entity.Role;
import com.LHZ.SETSS2026.entity.User;
import com.LHZ.SETSS2026.security.JwtUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final JwtUtil jwtUtil;
    private static final String prefix = "user:roles:";
    final private StringRedisTemplate stringRedisTemplate;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleRepository roleRepository, JwtUtil jwtUtil, StringRedisTemplate stringRedisTemplate) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.jwtUtil = jwtUtil;
        this.stringRedisTemplate = stringRedisTemplate;
    }


    // 用户注册业务
    @Transactional
    public String register(RegisterRequest request) {
        Optional<User> existingUser = userRepository.findByName(request.getUsername()).stream().findFirst();
        if (existingUser.isPresent()) {
            return "用户名已存在";
        }

        if (request.getEmail() != null && userRepository.findByEmail(request.getEmail()).isPresent()) {
            return "邮箱已被注册";
        }

        if (request.getPhone() != null && userRepository.findByPhone(request.getPhone()).isPresent()) {
            return "手机号已被注册";
        }

        // 创建用户
        User user = request.toEntity();
        user.setPassword(passwordEncoder.encode(user.getPassword()));

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

        user.setIsOnline(true);
        userRepository.save(user);

        String onlineKey = "user:online:" + user.getId();
        stringRedisTemplate.opsForValue().set(onlineKey, "1", 60, TimeUnit.MINUTES);

        String token = jwtUtil.generateToken(String.valueOf(user.getId()), user.getName());
        return new AuthResponse(token, user.getId());
    }

    public UserInfoResponse getUserInfoByTokrn(String token){ //根据token判断
        UserInfoResponse userInfo = new UserInfoResponse();
        if(! jwtUtil.validateToken(token)){
            userInfo.setError("invalid token");
            return userInfo;
        }
        userInfo.setUserName(jwtUtil.extractUserName(token));
        User user = userRepository.findByName(userInfo.getUserName()).getFirst();
        userInfo.setEmail(user.getEmail());
        userInfo.setPhone(user.getPhone());
        userInfo.setEnable(user.getEnable());
        userInfo.setRole(user.getRole().getName());

        return userInfo;

    }

    public boolean checkToken(Integer userId, String token) {
        if (!jwtUtil.validateToken(token)) {
            return false;
        }

        String extractedId = jwtUtil.extractId(token);
        if (extractedId == null || !extractedId.equals(String.valueOf(userId))) {
            return false;
        }

        String onlineKey = "user:online:" + userId;

        return stringRedisTemplate.hasKey(onlineKey);
    }

}
