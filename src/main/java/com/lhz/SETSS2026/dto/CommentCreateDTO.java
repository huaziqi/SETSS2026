package com.LHZ.SETSS2026.dto;

import lombok.Data;

@Data
public class CommentCreateDTO {
    private String content;
    private Long parentCommentId;
}
