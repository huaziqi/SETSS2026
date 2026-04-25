package com.LHZ.SETSS2026.service;

import com.LHZ.SETSS2026.dto.CommentCreateDTO;
import com.LHZ.SETSS2026.dto.CommentDTO;
import com.LHZ.SETSS2026.entity.Comment;
import com.LHZ.SETSS2026.entity.Post;
import com.LHZ.SETSS2026.entity.User;
import com.LHZ.SETSS2026.repository.CommentRepository;
import com.LHZ.SETSS2026.repository.PostRepository;
import com.LHZ.SETSS2026.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public CommentService(CommentRepository commentRepository,
                          PostRepository postRepository,
                          UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public CommentDTO createComment(Long postId, Integer userId, CommentCreateDTO dto) {
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

        Comment savedComment = commentRepository.save(comment);

        long commentCount = commentRepository.countByPost_PostId(postId);
        post.setCommentCount((int) commentCount);

        return CommentDTO.fromEntity(savedComment);
    }

    public List<CommentDTO> getCommentsByPost(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new EntityNotFoundException("Post not found");
        }

        List<Comment> commentList = commentRepository.findByPost_PostIdOrderByPublishTimeAsc(postId);
        Map<Long, CommentDTO> dtoMap = new LinkedHashMap<>();
        List<CommentDTO> rootComments = new ArrayList<>();

        for (Comment comment : commentList) {
            dtoMap.put(comment.getCommentId(), CommentDTO.fromEntity(comment));
        }

        for (Comment comment : commentList) {
            CommentDTO currentDto = dtoMap.get(comment.getCommentId());
            if (comment.getParentComment() == null) {
                rootComments.add(currentDto);
            } else {
                CommentDTO parent = dtoMap.get(comment.getParentComment().getCommentId());
                if (parent != null) {
                    parent.getReplies().add(currentDto);
                } else {
                    rootComments.add(currentDto);
                }
            }
        }

        return rootComments;
    }
}
