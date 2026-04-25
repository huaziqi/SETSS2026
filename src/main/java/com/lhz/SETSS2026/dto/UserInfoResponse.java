package com.LHZ.SETSS2026.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class UserInfoResponse {

    private String userName;
    private String email;
    private String phone;
    private boolean enable;
    private String error;
    private String role;


}
