package com.LHZ.SETSS2026.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "review_record")
public class ReviewRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Integer recordId;

    // 审稿时间
    private LocalDateTime reviewTime;

    // 审稿员ID（对应用户表）
    private Integer reviewerId;

    // 稿件ID
    private Integer manuscriptId;

    // 审稿结果：通过/驳回/需修改
    private String reviewResult;

    // 审稿意见
    @Column(columnDefinition = "text")
    private String reviewComment;

    // 审稿分数（可选）
}
