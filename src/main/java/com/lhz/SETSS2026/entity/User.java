package com.lhz.SETSS2026.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {
    private Long userId;
    private String userName;
    private String passWord;
    private String email;
    private Integer phoneNum;
}
