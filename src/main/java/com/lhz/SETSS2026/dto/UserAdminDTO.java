package com.LHZ.SETSS2026.dto;

import com.LHZ.SETSS2026.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserAdminDTO {

    private Integer id;
    private String name;
    private String email;
    private String phone;
    private Boolean enable;

    private Integer roleId;
    private String roleName;

    public static UserAdminDTO fromEntity(User user) {
        UserAdminDTO dto = new UserAdminDTO();

        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setEnable(user.getEnable());

        if (user.getRole() != null) {
            dto.setRoleId(user.getRole().getId());
            dto.setRoleName(user.getRole().getName());
        }

        return dto;
    }
}