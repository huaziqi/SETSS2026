package com.LHZ.SETSS2026.controller;

import com.LHZ.SETSS2026.dto.CommentCreateDTO;
import com.LHZ.SETSS2026.entity.User;
import com.LHZ.SETSS2026.repository.UserRepository;
import com.LHZ.SETSS2026.security.CustomUserDetails;
import com.LHZ.SETSS2026.service.CommentService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
public class CommentController {

    private final CommentService commentService;
    private final UserRepository userRepository;

    public CommentController(CommentService commentService, UserRepository userRepository) {
        this.commentService = commentService;
        this.userRepository = userRepository;
    }

    private Integer getCurrentUserId(CustomUserDetails userDetails) {
        User user = userRepository.findByName(userDetails.getUsername())
                .stream()
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return user.getId();
    }

    @GetMapping
    public ResponseEntity<?> listComments(@PathVariable Long postId) {
        try {
            return ResponseEntity.ok(commentService.getCommentsByPost(postId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createComment(@PathVariable Long postId,
                                           @RequestBody CommentCreateDTO dto,
                                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Integer userId = getCurrentUserId(userDetails);
            return ResponseEntity.ok(commentService.createComment(postId, userId, dto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
