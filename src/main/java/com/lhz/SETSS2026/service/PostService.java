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
    private final ArticleEmbeddingService articleEmbeddingService;

    public PostService(PostRepository postRepository,
                       UserRepository userRepository, ArticleEmbeddingService articleEmbeddingService) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.articleEmbeddingService = articleEmbeddingService;
    }

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

        if ("PUBLISHED".equalsIgnoreCase(savedPost.getStatus())) {
            articleEmbeddingService.syncPostEmbeddings(savedPost.getPostId());
        }

        return PostDTO.fromEntity(savedPost);
    }

    @Transactional
    public PostDTO updatePost(Long postId, Integer userId, PostDTO dto) {
        System.out.println("sync embeddings----------------");
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));

        if (!post.getUser().getId().equals(userId)) {
            throw new RuntimeException("No permission to update this post");
        }

        dto.updateEntity(post);
        Post savedPost = postRepository.save(post);

        if("PUBLISHED".equalsIgnoreCase(savedPost.getStatus())){
            System.out.println("sync embeddings");
            articleEmbeddingService.deleteEmbeddings("POST", post.getPostId());
            articleEmbeddingService.syncPostEmbeddings(post.getPostId());
        }

        if("DRAFT".equalsIgnoreCase(savedPost.getStatus())){
            articleEmbeddingService.deleteEmbeddings("POST", post.getPostId());
        }


        return PostDTO.fromEntity(savedPost);
    }

    @Transactional
    public void deletePost(Long postId, Integer userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));

        if (!post.getUser().getId().equals(userId)) {
            throw new RuntimeException("No permission to delete this post");
        }

        articleEmbeddingService.deleteEmbeddings("POST", post.getPostId());

        postRepository.delete(post);
    }

    public List<PostDTO> getPostsByUser(Integer userId) {
        return postRepository.findByUser_IdOrderByPublishTimeDesc(userId)
                .stream()
                .map(PostDTO::fromEntity)
                .toList();
    }

    public List<PostDTO> getPostList() {
        return postRepository.findAllByOrderByIsPinnedDescPublishTimeDesc()
                .stream()
                .map(PostDTO::fromEntity)
                .toList();
    }

    @Transactional
    public PostDTO getPostDetail(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));

        post.setViewCount(post.getViewCount() + 1);

        return PostDTO.fromEntity(post);
    }

    public PostDTO getPostDetailForOwner(Long postId, Integer userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));

        if (!post.getUser().getId().equals(userId)) {
            throw new RuntimeException("No permission to view this post");
        }

        return PostDTO.fromEntity(post);
    }

    @Transactional
    public void increaseViewCount(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));

        post.setViewCount(post.getViewCount() + 1);
    }
}
