package com.LHZ.SETSS2026.dto.semantic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TextChunk {

    private Integer index;

    private Integer start;
    private Integer end;

    private Integer blockStart;
    private Integer blockEnd;

    private String content;
}