package com.LHZ.SETSS2026.controller;


import com.LHZ.SETSS2026.dto.AuthRequest;
import com.LHZ.SETSS2026.dto.AuthResponse;
import com.LHZ.SETSS2026.dto.RegisterRequest;
import com.LHZ.SETSS2026.dto.UserInfoResponse;
import com.LHZ.SETSS2026.entity.User;
import com.LHZ.SETSS2026.service.JwtAuthService;
import com.LHZ.SETSS2026.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth") // 请求根路径
public class AuthController {
    private final UserService userService;
    private final JwtAuthService jwtAuthService;

    public AuthController(UserService userService, JwtAuthService jwtAuthService) {
        this.userService = userService;
        this.jwtAuthService = jwtAuthService;
    }
    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest request) {
        return userService.register(request);
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        System.out.println(request);
        try {
            AuthResponse response = userService.login(request.getUsername(), request.getPassword());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/validate")
    public UserInfoResponse validateUser(String token){
        return userService.getUserInfoByTokrn(token);
    }


}