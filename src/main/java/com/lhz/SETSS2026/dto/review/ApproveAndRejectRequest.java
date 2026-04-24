package com.LHZ.SETSS2026.dto.review;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApproveAndRejectRequest {
    private Integer manuscriptId;
    private Integer reviewerId;
    private String comment;
}
