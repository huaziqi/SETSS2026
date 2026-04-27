package com.LHZ.SETSS2026.entity;

import com.LHZ.SETSS2026.enums.ManuscriptGrade;
import com.LHZ.SETSS2026.enums.ManuscriptStatus;
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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer manuscriptId;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String introduction;

    private String author;

    private Integer userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50, nullable = false, columnDefinition = "VARCHAR(50)")
    private ManuscriptStatus status;

    private String originalFileName;

    private String filePath;

    private String fileType;

    private Long fileSize;

    private Integer reviewerId;

    private String reviewer;

    @Column(columnDefinition = "TEXT")
    private String reviewResult;

    @Enumerated(EnumType.STRING)
    @Column(name = "grade", length = 10, columnDefinition = "VARCHAR(10)")
    private ManuscriptGrade grade;

    private LocalDateTime publishTime;

    private LocalDateTime updateTime;
}
