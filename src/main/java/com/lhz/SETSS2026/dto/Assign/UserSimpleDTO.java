package com.LHZ.SETSS2026.dto.Assign;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSimpleDTO {
    private Integer id;
    private String name;
    private String email;
    private String phone;
    private String role;
}

