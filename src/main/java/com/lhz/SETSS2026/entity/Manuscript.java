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
@Table(name = "manuscript")
public class Manuscript {
    // 稿件ID 主键 自增
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer manuscriptId;

    // 稿件标题
    private String title;

    // 稿件内容 / 摘要
    @Column(columnDefinition = "TEXT") // 长文本
    private String introduction;

    // 作者姓名
    private String author;

    // 上传用户ID（对应用 User.userId）
    private Integer userId;

    // 稿件状态
    // 待审核 / 已分配 / 评审中 / 通过 / 驳回 / 已发布
    private String status;

    // 原始文件名
    private String originalFileName;

    // 文件存储路径（相对路径或绝对路径）
    private String filePath;

    // 文件类型（pdf/docx/zip）
    private String fileType;

    // 文件大小（字节）
    private Long fileSize;

    // 审稿员ID（可以存用户名/ID）
    private Integer reviewerId;

    // 审稿员
    private String reviewer;

    // 评审结果（意见）
    @Column(columnDefinition = "TEXT")
    private String reviewResult;

    // 提交时间
    private LocalDateTime publishTime;

    // 更新时间
    private LocalDateTime updateTime;
}
