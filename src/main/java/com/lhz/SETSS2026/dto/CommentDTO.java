package com.LHZ.SETSS2026.dto;

import com.LHZ.SETSS2026.entity.Comment;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class CommentDTO {
    private Long commentId;
    private Long postId;
    private Integer userId;
    private String userName;
    private Long parentCommentId;
    private String content;
    private LocalDateTime publishTime;
    private List<CommentDTO> replies = new ArrayList<>();

    public static CommentDTO fromEntity(Comment comment) {
        CommentDTO dto = new CommentDTO();
        dto.setCommentId(comment.getCommentId());
        dto.setPostId(comment.getPost().getPostId());
        dto.setUserId(comment.getUser().getId());
        dto.setUserName(comment.getUser().getName());
        dto.setParentCommentId(comment.getParentComment() != null ? comment.getParentComment().getCommentId() : null);
        dto.setContent(comment.getContent());
        dto.setPublishTime(comment.getPublishTime());
        return dto;
    }
}
