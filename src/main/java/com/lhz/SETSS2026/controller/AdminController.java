package com.LHZ.SETSS2026.controller;

import com.LHZ.SETSS2026.common.result.Result;
import com.LHZ.SETSS2026.dto.CommentDTO;
import com.LHZ.SETSS2026.dto.PostDTO;
import com.LHZ.SETSS2026.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

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

    @GetMapping("/posts")
    public List<PostDTO> listPosts() {
        return adminService.listAllPosts();
    }

    @PostMapping("/posts")
    public PostDTO createPost(@RequestBody PostDTO dto) {
        return adminService.createPostByAdmin(dto);
    }

    @PutMapping("/posts/{postId}")
    public PostDTO updatePost(@PathVariable Long postId, @RequestBody PostDTO dto) {
        return adminService.updatePostByAdmin(postId, dto);
    }

    @DeleteMapping("/posts/{postId}")
    public void deletePost(@PathVariable Long postId) {
        adminService.deletePostByAdmin(postId);
    }

    @GetMapping("/comments")
    public List<CommentDTO> listComments(@RequestParam(required = false) Long postId) {
        return adminService.listComments(postId);
    }

    @PostMapping("/comments")
    public CommentDTO createComment(@RequestParam Long postId,
                                    @RequestParam Integer userId,
                                    @RequestBody CommentDTO dto) {
        return adminService.createCommentByAdmin(postId, userId, dto);
    }

    @PutMapping("/comments/{commentId}")
    public CommentDTO updateComment(@PathVariable Long commentId, @RequestBody CommentDTO dto) {
        return adminService.updateCommentByAdmin(commentId, dto);
    }

    @DeleteMapping("/comments/{commentId}")
    public void deleteComment(@PathVariable Long commentId) {
        adminService.deleteCommentByAdmin(commentId);
    }
}
