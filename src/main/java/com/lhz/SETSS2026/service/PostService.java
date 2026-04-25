package com.LHZ.SETSS2026.service;

import com.LHZ.SETSS2026.dto.PostDTO;
import com.LHZ.SETSS2026.entity.Post;
import com.LHZ.SETSS2026.entity.User;
import com.LHZ.SETSS2026.repository.PostRepository;
import com.LHZ.SETSS2026.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public PostService(PostRepository postRepository,
                       UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    /**
     * 创建帖子 / 保存草稿
     */
    @Transactional
    public PostDTO createPost(PostDTO dto, Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Post post = dto.toEntity(user);

        if (post.getStatus() == null || post.getStatus().isBlank()) {
            post.setStatus("PUBLISHED");
        }

        post.setCommentCount(0);
        post.setViewCount(0);
        post.setIsPinned(false);

        Post savedPost = postRepository.save(post);

        return PostDTO.fromEntity(savedPost);
    }

    /**
     * 更新帖子
     */
    @Transactional
    public PostDTO updatePost(Long postId, Integer userId, PostDTO dto) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));

        if (!post.getUser().getId().equals(userId)) {
            throw new RuntimeException("No permission to update this post");
        }

        dto.updateEntity(post);

        Post savedPost = postRepository.save(post);

        return PostDTO.fromEntity(savedPost);
    }

    /**
     * 查询某个用户所有帖子
     */
    public List<PostDTO> getPostsByUser(Integer userId) {
        return postRepository.findByUser_IdOrderByPublishTimeDesc(userId)
                .stream()
                .map(PostDTO::fromEntity)
                .toList();
    }

    /**
     * 获取帖子列表
     */
    public List<PostDTO> getPostList() {
        return postRepository.findAllByOrderByIsPinnedDescPublishTimeDesc()
                .stream()
                .map(PostDTO::fromEntity)
                .toList();
    }

    /**
     * 获取帖子详情，并让浏览量 +1
     */
    @Transactional
    public PostDTO getPostDetail(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));

        post.setViewCount(post.getViewCount() + 1);

        return PostDTO.fromEntity(post);
    }

    /**
     * 单独浏览量 +1
     */
    @Transactional
    public void increaseViewCount(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));

        post.setViewCount(post.getViewCount() + 1);
    }
}