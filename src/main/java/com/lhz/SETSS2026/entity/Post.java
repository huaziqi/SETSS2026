package com.LHZ.SETSS2026.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "post")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long postId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(name = "content", columnDefinition = "LONGTEXT")
    private String content;

    @Column(name = "publish_time")
    private LocalDateTime publishTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @Column
    private String tag;

    @Column(nullable = false)
    private String status = "PUBLISHED";

    @Column(name = "comment_count", nullable = false)
    private Integer commentCount = 0;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

    @Column(name = "is_pinned", nullable = false)
    private Boolean isPinned = false;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        publishTime = now;
        updateTime = now;
    }

    @PreUpdate
    public void preUpdate() {
        updateTime = LocalDateTime.now();
    }
}