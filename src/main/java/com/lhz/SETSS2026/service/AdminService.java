package com.LHZ.SETSS2026.service;

import com.LHZ.SETSS2026.entity.Role;
import com.LHZ.SETSS2026.entity.User;
import com.LHZ.SETSS2026.repository.RoleRepository;
import com.LHZ.SETSS2026.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    //查询所有用户（管理员）
    public List<User> listAllUsers(){
        return userRepository.findAll();
    }

    //禁用用户
    public void disableUser(Integer userId){
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setEnable(false);
        userRepository.save(user);
    }

    //启用用户
    public void enableUser(Integer userId){
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setEnable(true);
        userRepository.save(user);
    }

    //给用户分配角色
    public void assignRoleToUser(Integer userId, Integer roleId){
        User user = userRepository.findById(userId).orElseThrow();
        Role role = roleRepository.findById(roleId).orElseThrow();
        user.setRole(role);
        userRepository.save(user);
    }


}
