package com.LHZ.SETSS2026.controller;

import com.LHZ.SETSS2026.common.result.Result;
import com.LHZ.SETSS2026.dto.review.AssignManuscriptRequest;
import com.LHZ.SETSS2026.entity.Manuscript;
import com.LHZ.SETSS2026.service.ChairService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chair")
@RequiredArgsConstructor
public class ChairController {

    private final ChairService chairService;

    // ================= 用户管理 =================

    @GetMapping("/user/list")
    public Result listUsers() {
        return Result.success(chairService.listAllUsers());
    }

    @PostMapping("/user/disable/{userId}")
    public Result disableUser(@PathVariable Integer userId) {
        try {
            chairService.disableUser(userId);
            return Result.success("禁用成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/user/enable/{userId}")
    public Result enableUser(@PathVariable Integer userId) {
        try {
            chairService.enableUser(userId);
            return Result.success("启用成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/user/assignRole")
    public Result assignRoleToUser(@RequestParam Integer userId,
                                   @RequestParam Integer roleId) {
        try {
            chairService.assignRoleToUser(userId, roleId);
            return Result.success("分配角色成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    // ================= 稿件管理 =================

    @PostMapping("/manuscript/assign")
    public Result assignManuscript(@RequestBody AssignManuscriptRequest request) {
        try {
            Manuscript manuscript = chairService.assignManuscript(
                    request.getManuscriptId(),
                    request.getReviewerId()
            );
            return Result.success("稿件分配成功", manuscript);
        } catch (Exception e) {
            return Result.error("稿件分配失败：" + e.getMessage());
        }
    }

    @GetMapping("/manuscript/pending")
    public Result getPendingManuscripts() {
        try {
            List<Manuscript> list = chairService.getPendingManuscripts();
            return Result.success(list);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/manuscript/assigned")
    public Result getAssignedManuscripts() {
        try {
            List<Manuscript> list = chairService.getAssignedManuscripts();
            return Result.success(list);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/manuscript/reviewer/{reviewerId}")
    public Result getManuscriptsByReviewerId(@PathVariable Integer reviewerId) {
        try {
            List<Manuscript> list = chairService.getManuscriptsByReviewerId(reviewerId);
            return Result.success(list);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }
}