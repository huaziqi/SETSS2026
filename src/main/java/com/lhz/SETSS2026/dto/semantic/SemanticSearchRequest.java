package com.LHZ.SETSS2026.dto.semantic;

import lombok.Data;

@Data
public class SemanticSearchRequest {
    private String keyword;
    private Integer limit;
}