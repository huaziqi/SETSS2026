package com.LHZ.SETSS2026.dto;

import com.LHZ.SETSS2026.entity.Post;
import com.LHZ.SETSS2026.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PostDTO {

    private Long postId;

    private Integer userId;
    private String userName;

    private String title;
    private String content;
    private String tag;

    private String status;

    private Integer commentCount;
    private Integer viewCount;
    private Boolean isPinned;

    private LocalDateTime publishTime;
    private LocalDateTime updateTime;

    private String htmlContent;
    public Post toEntity(User user) {
        Post post = new Post();

        post.setUser(user);
        post.setTitle(this.title);
        post.setContent(this.content);
        post.setTag(this.tag);

        if (this.status != null && !this.status.isBlank()) {
            post.setStatus(this.status);
        }

        return post;
    }
    public static PostDTO fromEntity(Post post) {
        PostDTO dto = new PostDTO();

        dto.setPostId(post.getPostId());

        dto.setUserId(post.getUser().getId());
        dto.setUserName(post.getUser().getName());

        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setTag(post.getTag());

        dto.setStatus(post.getStatus());

        dto.setCommentCount(post.getCommentCount());
        dto.setViewCount(post.getViewCount());
        dto.setIsPinned(post.getIsPinned());

        dto.setPublishTime(post.getPublishTime());
        dto.setUpdateTime(post.getUpdateTime());

        return dto;
    }
    public void updateEntity(Post post) {

        if (this.title != null) {
            post.setTitle(this.title);
        }

        if (this.content != null) {
            post.setContent(this.content);
        }

        if (this.tag != null) {
            post.setTag(this.tag);
        }

        if (this.status != null && !this.status.isBlank()) {
            post.setStatus(this.status);
        }
    }
}