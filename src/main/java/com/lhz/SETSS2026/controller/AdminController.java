package com.LHZ.SETSS2026.controller;

import com.LHZ.SETSS2026.common.result.Result;
import com.LHZ.SETSS2026.dto.ConferencePageDTO;
import com.LHZ.SETSS2026.service.AdminService;
import com.LHZ.SETSS2026.service.ConferencePageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final ConferencePageService conferencePageService;

    // ================= 用户管理 =================

    @GetMapping("/user/list")
    public Result listUsers(Principal principal) {
        return Result.success(adminService.listAllUsers(principal.getName()));
    }

    @PostMapping("/user/disable/{userId}")
    public Result disableUser(@PathVariable Integer userId, Principal principal) {
        adminService.disableUser(userId, principal.getName());
        return Result.success("禁用成功");
    }

    @PostMapping("/user/enable/{userId}")
    public Result enableUser(@PathVariable Integer userId, Principal principal) {
        adminService.enableUser(userId, principal.getName());
        return Result.success("启用成功");
    }

    @PostMapping("/user/assignRole")
    public Result assignRoleToUser(@RequestParam Integer userId,
                                   @RequestParam Integer roleId,
                                   Principal principal) {
        adminService.assignRoleToUser(userId, roleId, principal.getName());
        return Result.success("分配角色成功");
    }

    // ================= 帖子管理 =================

    @GetMapping("/posts")
    public Result listPosts() {
        return Result.success(adminService.listAllPosts());
    }

    @PutMapping("/posts/{postId}/pin")
    public Result togglePin(@PathVariable Long postId,
                            @RequestParam Boolean isPinned) {
        adminService.togglePin(postId, isPinned);
        return Result.success("Pin status updated");
    }

    @DeleteMapping("/posts/{postId}")
    public Result deletePost(@PathVariable Long postId) {
        adminService.deletePostByAdmin(postId);
        return Result.success("Post deleted");
    }

    // ================= 评论管理 =================

    @GetMapping("/comments")
    public Result listComments(@RequestParam(required = false) Long postId) {
        return Result.success(adminService.listComments(postId));
    }

    @DeleteMapping("/comments/{commentId}")
    public Result deleteComment(@PathVariable Long commentId) {
        adminService.deleteCommentByAdmin(commentId);
        return Result.success("Comment deleted");
    }

    // ================= 会议信息页面管理 =================

    @GetMapping("/pages")
    public Result listPages() {
        return Result.success(conferencePageService.listPages());
    }

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
        return conferencePageService.getPageByKey(pageKey);
    }

    @PutMapping("/pages/{pageKey}")
    public Result updatePage(@PathVariable String pageKey,
                             @RequestBody ConferencePageDTO dto) {
        return Result.success(conferencePageService.updatePage(pageKey, dto));
    }
}