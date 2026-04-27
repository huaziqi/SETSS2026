package com.LHZ.SETSS2026.service;

import com.LHZ.SETSS2026.dto.semantic.TextChunk;
import com.LHZ.SETSS2026.util.SemanticTool;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SemanticMarkdownChunkService {

    private static final int MAX_CHARS_PER_CHUNK = 600;

    public List<TextChunk> splitMarkdown(String markdown) {
        return splitMarkdown(markdown, MAX_CHARS_PER_CHUNK);
    }

    public List<TextChunk> splitMarkdown(String markdown, int maxChars) {
        Parser parser = Parser.builder().build();
        Node doc = parser.parse(markdown);

        List<TextChunk> out = new ArrayList<>();
        int idx = 0;
        int blockIndex = 0;

        for (Node block = doc.getFirstChild(); block != null; block = block.getNext()) {
            BasedSequence seq = block.getChars();

            if (seq == null || seq.isEmpty()) {
                continue;
            }

            String raw = seq.toString();

            if (raw.trim().isEmpty()) {
                continue;
            }

            int blockStart = block.getStartOffset();
            int blockEnd = block.getEndOffset();

            if (raw.length() <= maxChars) {
                out.add(new TextChunk(
                        idx++,
                        blockStart,
                        blockEnd,
                        blockIndex,
                        blockIndex,
                        raw.trim()
                ));
            } else {
                List<TextChunk> parts =
                        SemanticTool.splitLongBlockBySentence(block, maxChars, idx);

                for (TextChunk part : parts) {
                    part.setBlockStart(blockIndex);
                    part.setBlockEnd(blockIndex);
                }

                out.addAll(parts);
                idx = out.size();
            }

            blockIndex++;
        }

        return out;
    }
}
