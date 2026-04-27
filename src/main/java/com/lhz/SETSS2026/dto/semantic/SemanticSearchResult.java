package com.LHZ.SETSS2026.dto.semantic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SemanticSearchResult {

    private String sourceType;
    private Long sourceId;
    private String pageKey;
    private String title;
    private String url;

    private Integer chunkIndex;
    private String anchorId;

    private Integer charStart;
    private Integer charEnd;
    private Integer blockStart;
    private Integer blockEnd;

    private String content;

    private Double distance;
    private Double similarity;
}