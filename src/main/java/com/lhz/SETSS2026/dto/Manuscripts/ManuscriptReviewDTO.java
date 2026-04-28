package com.LHZ.SETSS2026.dto.Manuscripts;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ManuscriptReviewDTO {
    private Integer manuscriptId;
    private String title;
    private String author;
    private String introduction;
    private String originalFileName;
    private LocalDateTime publishTime;
    private LocalDateTime updateTime;
    private String status;
    private String reviewer;
    private String reviewResult;
    private String grade;
}
