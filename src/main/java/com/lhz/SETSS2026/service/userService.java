package com.lhz.SETSS2026.service;

import com.lhz.SETSS2026.dto.AuthResponse;
import com.lhz.SETSS2026.entity.User;

public class UserService {
    public String register(User user) {
        return null;
    }

    public AuthResponse login(String username, String password) {
        if (username.equals("admin") && password.equals("123")){
            return new AuthResponse("0");
        }
        return new AuthResponse("1");
    }
}
