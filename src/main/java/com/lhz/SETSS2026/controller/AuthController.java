package com.LHZ.SETSS2026.controller;


import com.LHZ.SETSS2026.dto.AuthRequest;
import com.LHZ.SETSS2026.dto.AuthResponse;
import com.LHZ.SETSS2026.entity.User;
import com.LHZ.SETSS2026.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth") // 请求根路径
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }
    @PostMapping("/register")
    public String register(@RequestBody User user) {
        return userService.register(user);
    }
    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest request) {
        return userService.login(request.getUsername(), request.getPassword());
    }
}
