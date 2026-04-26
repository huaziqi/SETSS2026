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

    //查询所有用户（管理员）
    public List<UserAdminDTO> listAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserAdminDTO::fromEntity)
                .toList();
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

    public List<CommentDTO> listComments(Long postId) {
        List<Comment> comments = postId == null
                ? commentRepository.findAllByOrderByPublishTimeDesc()
                : commentRepository.findByPost_PostIdOrderByPublishTimeDesc(postId);

        return comments.stream().map(CommentDTO::fromEntity).toList();
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
