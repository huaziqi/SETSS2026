package com.LHZ.SETSS2026.controller;

import com.LHZ.SETSS2026.common.result.Result;
import com.LHZ.SETSS2026.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final com.LHZ.SETSS2026.service.AdminService adminService;

    // 查询所有用户
    @GetMapping("/user/list")
    public Result listUsers() {
        return Result.success(adminService.listAllUsers());
    }

    //禁用用户
    @PostMapping("/user/disable/{userId}")
    public Result disableUser(@PathVariable Integer userId) {
        adminService.disableUser(userId);
        return Result.success("禁用成功");
    }

    //启用用户
    @PostMapping("/user/enable/{userId}")
    public Result enableUser(@PathVariable Integer userId) {
        adminService.enableUser(userId);
        return Result.success("启用成功");
    }

    //分配角色
    @PostMapping("/user/assignRole")
    public Result assignRoleToUser(@RequestParam Integer userId, @RequestParam Integer roleId) {
        adminService.assignRoleToUser(userId, roleId);
        return Result.success("分配角色成功");
    }
}