package com.LHZ.SETSS2026.service;

import com.LHZ.SETSS2026.dto.CommentDTO;
import com.LHZ.SETSS2026.dto.PostDTO;
import com.LHZ.SETSS2026.dto.UserAdminDTO;
import com.LHZ.SETSS2026.entity.Comment;
import com.LHZ.SETSS2026.entity.Post;
import com.LHZ.SETSS2026.entity.Role;
import com.LHZ.SETSS2026.entity.User;
import com.LHZ.SETSS2026.repository.CommentRepository;
import com.LHZ.SETSS2026.repository.PostRepository;
import com.LHZ.SETSS2026.repository.RoleRepository;
import com.LHZ.SETSS2026.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    private static final String ROLE_USER = "ROLE_USER";
    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_CHAIR = "ROLE_CHAIR";
    private static final String ROLE_REVIEWER = "ROLE_REVIEWER";

    // ================= 用户管理 =================

    public List<UserAdminDTO> listAllUsers(String operatorName) {
        User operator = getOperator(operatorName);
        String operatorRole = getRoleName(operator);

        return userRepository.findAll()
                .stream()
                .filter(user -> canViewUser(operatorRole, user))
                .map(UserAdminDTO::fromEntity)
                .toList();
    }

    public void disableUser(Integer userId, String operatorName) {
        User operator = getOperator(operatorName);
        User target = getTargetUser(userId);

        checkCanManageUser(operator, target);

        target.setEnable(false);
        userRepository.save(target);
    }

    public void enableUser(Integer userId, String operatorName) {
        User operator = getOperator(operatorName);
        User target = getTargetUser(userId);

        checkCanManageUser(operator, target);

        target.setEnable(true);
        userRepository.save(target);
    }

    public void assignRoleToUser(Integer userId, Integer roleId, String operatorName) {
        User operator = getOperator(operatorName);
        User target = getTargetUser(userId);

        Role newRole = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("角色不存在"));

        checkCanManageUser(operator, target);
        checkCanAssignRole(operator, target, newRole);

        target.setRole(newRole);
        userRepository.save(target);
    }

    private User getOperator(String operatorName) {
        return userRepository.findByNameWithRole(operatorName)
                .orElseThrow(() -> new RuntimeException("当前登录用户不存在"));
    }

    private User getTargetUser(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    private String getRoleName(User user) {
        if (user == null || user.getRole() == null || user.getRole().getName() == null) {
            return "";
        }

        return user.getRole().getName();
    }

    private boolean canViewUser(String operatorRole, User target) {
        String targetRole = getRoleName(target);

        if (ROLE_CHAIR.equals(operatorRole)) {
            return true;
        }

        if (ROLE_ADMIN.equals(operatorRole) || ROLE_REVIEWER.equals(operatorRole)) {
            return ROLE_USER.equals(targetRole);
        }

        return false;
    }

    private void checkCanManageUser(User operator, User target) {
        String operatorRole = getRoleName(operator);
        String targetRole = getRoleName(target);

        if (operator.getId().equals(target.getId())) {
            throw new RuntimeException("不能修改自己的权限或状态");
        }

        if (ROLE_CHAIR.equals(operatorRole)) {
            return;
        }

        if ((ROLE_ADMIN.equals(operatorRole) || ROLE_REVIEWER.equals(operatorRole))
                && ROLE_USER.equals(targetRole)) {
            return;
        }

        throw new RuntimeException("你没有权限管理该用户");
    }

    private void checkCanAssignRole(User operator, User target, Role newRole) {
        String operatorRole = getRoleName(operator);
        String newRoleName = newRole.getName();

        if (operator.getId().equals(target.getId())) {
            throw new RuntimeException("不能修改自己的权限");
        }

        if (ROLE_CHAIR.equals(operatorRole)) {
            return;
        }

        if ((ROLE_ADMIN.equals(operatorRole) || ROLE_REVIEWER.equals(operatorRole))
                && ROLE_USER.equals(newRoleName)) {
            return;
        }

        throw new RuntimeException("你没有权限分配该角色");
    }

    // ================= 帖子管理 =================

    public List<PostDTO> listAllPosts() {
        return postRepository.findAllByOrderByIsPinnedDescPublishTimeDesc()
                .stream()
                .map(PostDTO::fromEntity)
                .toList();
    }

    @Transactional
    public void togglePin(Long postId, Boolean isPinned) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        post.setIsPinned(isPinned);
        postRepository.save(post);
    }

    @Transactional
    public void deletePostByAdmin(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));

        List<Comment> comments = commentRepository.findByPost_PostIdOrderByPublishTimeAsc(postId);
        commentRepository.deleteAll(comments);
        postRepository.delete(post);
    }

    // ================= 评论管理 =================

    public List<CommentDTO> listComments(Long postId) {
        List<Comment> comments = postId == null
                ? commentRepository.findAllByOrderByPublishTimeDesc()
                : commentRepository.findByPost_PostIdOrderByPublishTimeDesc(postId);

        return comments.stream()
                .map(CommentDTO::fromEntity)
                .toList();
    }

    @Transactional
    public CommentDTO createCommentByAdmin(Long postId, Integer userId, CommentDTO dto) {
        if (dto.getContent() == null || dto.getContent().isBlank()) {
            throw new RuntimeException("Comment content cannot be empty");
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Comment comment = new Comment();
        comment.setPost(post);
        comment.setUser(user);
        comment.setContent(dto.getContent().trim());

        if (dto.getParentCommentId() != null) {
            Comment parent = commentRepository.findById(dto.getParentCommentId())
                    .orElseThrow(() -> new EntityNotFoundException("Parent comment not found"));

            if (!parent.getPost().getPostId().equals(postId)) {
                throw new RuntimeException("Parent comment does not belong to this post");
            }

            comment.setParentComment(parent);
        }

        Comment saved = commentRepository.save(comment);
        refreshPostCommentCount(post);

        return CommentDTO.fromEntity(saved);
    }

    @Transactional
    public CommentDTO updateCommentByAdmin(Long commentId, CommentDTO dto) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found"));

        if (dto.getContent() != null && !dto.getContent().isBlank()) {
            comment.setContent(dto.getContent().trim());
        }

        return CommentDTO.fromEntity(commentRepository.save(comment));
    }

    @Transactional
    public void deleteCommentByAdmin(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found"));

        Post post = comment.getPost();

        List<Comment> toDelete = new ArrayList<>();
        collectCommentTree(comment, toDelete);

        commentRepository.deleteAll(toDelete);
        refreshPostCommentCount(post);
    }

    private void collectCommentTree(Comment root, List<Comment> collector) {
        List<Comment> children = commentRepository.findByParentComment_CommentId(root.getCommentId());

        for (Comment child : children) {
            collectCommentTree(child, collector);
        }

        collector.add(root);
    }

    private void refreshPostCommentCount(Post post) {
        long count = commentRepository.countByPost_PostId(post.getPostId());
        post.setCommentCount((int) count);
        postRepository.save(post);
    }
}