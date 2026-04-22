package com.LHZ.SETSS2026.service;

import com.LHZ.SETSS2026.dto.semantic.TextChunk;

import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.sequence.BasedSequence;

import java.util.ArrayList;
import java.util.List;

public class SemanticService {
    private static final int MAX_CHARS_PER_CHUNK = 600;
    private static final int MIN_CHARS_PER_CHUNK = 100;

    public static List<TextChunk> splitMarkdown(String markdown, int maxChars) {
        Parser parser = Parser.builder().build();
        Node doc = parser.parse(markdown);


        List<TextChunk> out = new ArrayList<>();
        int idx = 0;

        for (Node block = doc.getFirstChild(); block != null; block = block.getNext()) {
            BasedSequence seq = block.getChars();
            if (seq == null || seq.isEmpty()) continue;

            String raw = seq.toString();
            if (raw.trim().isEmpty()) continue;

            int blockStart = block.getStartOffset();
            int blockEnd = block.getEndOffset();

//            if (raw.length() <= maxChars) {
//                out.add(new TextChunk(
//                        idx++,
//                        blockStart,
//                        blockEnd,
//                        blockStart,
//                        blockEnd,
//                        raw.trim()
//                ));
//            } else {
//                List<TextChunk> parts =
//                        SemanticTool.splitLongBlockBySentence(block, maxChars, idx);
//                out.addAll(parts);
//                idx = out.size();
//            }
        }

        return out;
    }
}
