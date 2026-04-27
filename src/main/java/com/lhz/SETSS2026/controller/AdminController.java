package com.LHZ.SETSS2026.controller;

import com.LHZ.SETSS2026.common.result.Result;
import com.LHZ.SETSS2026.dto.ConferencePageDTO;
import com.LHZ.SETSS2026.service.AdminService;
import com.LHZ.SETSS2026.service.ConferencePageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final ConferencePageService conferencePageService;

    // ================= 用户管理 =================

    //查询所有用户
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
    public Result assignRoleToUser(@RequestParam Integer userId,
                                   @RequestParam Integer roleId) {
        adminService.assignRoleToUser(userId, roleId);
        return Result.success("分配角色成功");
    }

    // ================= 帖子管理 =================

    //查询所有帖子
    @GetMapping("/posts")
    public Result listPosts() {
        return Result.success(adminService.listAllPosts());
    }

    // Pin/Unpin
    @PutMapping("/posts/{postId}/pin")
    public Result togglePin(@PathVariable Long postId,
                            @RequestParam Boolean isPinned) {
        adminService.togglePin(postId, isPinned);
        return Result.success("Pin status updated");
    }

    // 删除帖子
    @DeleteMapping("/posts/{postId}")
    public Result deletePost(@PathVariable Long postId) {
        adminService.deletePostByAdmin(postId);
        return Result.success("Post deleted");
    }

    // ================= 评论管理 =================

    // 查询所有评论
    @GetMapping("/comments")
    public Result listComments(@RequestParam(required = false) Long postId) {
        return Result.success(adminService.listComments(postId));
    }

    // 删除评论
    @DeleteMapping("/comments/{commentId}")
    public Result deleteComment(@PathVariable Long commentId) {
        adminService.deleteCommentByAdmin(commentId);
        return Result.success("Comment deleted");
    }

    // ================= 会议信息页面管理 =================

    // 查询所有页面（后台列表）
    @GetMapping("/pages")
    public Result listPages() {
        return Result.success(conferencePageService.listPages());
    }

    // 查询单个页面（用于编辑）
    @GetMapping("/pages/{pageKey}")
    public Result getPage(@PathVariable String pageKey) {
        return Result.success(conferencePageService.getPageByKey(pageKey));
    }

    @GetMapping("/{pageKey}")
    public ConferencePageDTO getPageByKey(
            @PathVariable String pageKey,
            @RequestParam(required = false) String anchorId,
            @RequestParam(required = false) Integer blockStart,
            @RequestParam(required = false) Integer blockEnd
    ) {
        return conferencePageService.getPageByKey(pageKey, anchorId, blockStart, blockEnd);
    }

    // 更新页面内容（核心接口）
    @PutMapping("/pages/{pageKey}")
    public Result updatePage(@PathVariable String pageKey,
                             @RequestBody ConferencePageDTO dto) {
        return Result.success(conferencePageService.updatePage(pageKey, dto));
    }






}