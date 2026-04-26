package com.LHZ.SETSS2026.dto.review;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignManuscriptRequest {
    private Integer manuscriptId;
    private Integer reviewerId;
}
