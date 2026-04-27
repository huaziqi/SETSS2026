package com.LHZ.SETSS2026.controller;

import com.LHZ.SETSS2026.dto.PostDTO;
import com.LHZ.SETSS2026.entity.User;
import com.LHZ.SETSS2026.repository.UserRepository;
import com.LHZ.SETSS2026.security.CustomUserDetails;
import com.LHZ.SETSS2026.service.PostService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;
    private final UserRepository userRepository;

    public PostController(PostService postService, UserRepository userRepository) {
        this.postService = postService;
        this.userRepository = userRepository;
    }

    private Integer getCurrentUserId(CustomUserDetails userDetails) {
        User user = userRepository.findByName(userDetails.getUsername())
                .stream()
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return user.getId();
    }

    @PostMapping
    public ResponseEntity<?> createPost(@RequestBody PostDTO dto,
                                        @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Integer userId = getCurrentUserId(userDetails);
            return ResponseEntity.ok(postService.createPost(dto, userId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{postId}")
    public ResponseEntity<?> updatePost(@PathVariable Long postId,
                                        @RequestBody PostDTO dto,
                                        @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Integer userId = getCurrentUserId(userDetails);
            return ResponseEntity.ok(postService.updatePost(postId, userId, dto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable Long postId,
                                        @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Integer userId = getCurrentUserId(userDetails);
            postService.deletePost(postId, userId);
            return ResponseEntity.ok(Map.of("message", "Post deleted"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/my")
    public ResponseEntity<List<PostDTO>> getMyPosts(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Integer userId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(postService.getPostsByUser(userId));
    }

    @GetMapping("/my/{postId}")
    public ResponseEntity<?> getMyPostDetail(@PathVariable Long postId,
                                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Integer userId = getCurrentUserId(userDetails);
            return ResponseEntity.ok(postService.getPostDetailForOwner(postId, userId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<PostDTO>> getAllPosts() {
        return ResponseEntity.ok(postService.getPostList());
    }

    @GetMapping("/{postId}")
    public PostDTO getPostDetail(
            @PathVariable Long postId,
            @RequestParam(required = false) String anchorId,
            @RequestParam(required = false) Integer blockStart,
            @RequestParam(required = false) Integer blockEnd
    ) {
        return postService.getPostDetail(postId);
    }

}
