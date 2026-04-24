package com.LHZ.SETSS2026.dto;

import com.LHZ.SETSS2026.entity.User;
import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String email;
    private String phone;

    public User toEntity(){
        User user = new User();
        user.setName(this.username);
        user.setPassword(this.password);
        user.setEmail(this.email);
        user.setPhone(this.phone);
        user.setEnable(true);
        return user;
    }
}
