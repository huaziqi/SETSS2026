package com.LHZ.SETSS2026.util;


import com.LHZ.SETSS2026.dto.semantic.TextChunk;
import com.vladsch.flexmark.ast.*;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.ast.NodeVisitor;
import com.vladsch.flexmark.util.ast.VisitHandler;
import com.vladsch.flexmark.util.sequence.BasedSequence;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SemanticTool {

    // 句末标点（刻意不含 '.'，避免 URL / 小数误切）
    private static final String SENTENCE_END = "。！？?!；;";



    public static List<TextChunk> splitLongBlockBySentence(
            Node blockNode,
            int maxChars,
            int chunkIndexStart
    ) {
        BasedSequence blockSeq = blockNode.getChars();
        String blockText = blockSeq.toString();

        int blockStart = blockNode.getStartOffset();
        int blockEnd   = blockNode.getEndOffset();

        // 收集不可切分区间
        List<Range> protectedRanges =
                collectProtectedInlineRanges(blockNode, blockStart);

        // 句子级切分
        List<Range> sentenceRanges =
                splitBySentenceBoundaries(blockText, protectedRanges);

        List<TextChunk> out = new ArrayList<>();
        int idx = chunkIndexStart;

        int curStart = 0;
        int curEnd = 0;

        for (Range sr : sentenceRanges) {
            if (curStart == curEnd) {
                curStart = sr.start;
                curEnd = sr.end;
            } else {
                int newLen = sr.end - curStart;
                if (newLen <= maxChars) {
                    curEnd = sr.end;
                } else {
                    out.add(buildChunk(
                            blockText,
                            blockStart,
                            blockEnd,
                            curStart,
                            curEnd,
                            idx++
                    ));
                    curStart = sr.start;
                    curEnd = sr.end;
                }
            }
        }

        if (curStart < curEnd) {
            out.add(buildChunk(
                    blockText,
                    blockStart,
                    blockEnd,
                    curStart,
                    curEnd,
                    idx++
            ));
        }

        return forceSplitIfStillTooLong(out, maxChars);
    }

    /* ===================== Chunk 构造（唯一入口） ===================== */

    public static TextChunk buildChunk(
            String blockText,
            int blockStart,
            int blockEnd,
            int relStart,
            int relEnd,
            int index
    ) {
        int absStart = blockStart + relStart;
        int absEnd = blockStart + relEnd;
        String content = blockText.substring(relStart, relEnd).trim();

        return new TextChunk(
                index,
                absStart,
                absEnd,
                blockStart,
                blockEnd,
                content
        );
    }

    /* ===================== Inline 保护区 ===================== */

    public static List<Range> collectProtectedInlineRanges(
            Node root,
            int blockStartOffset
    ) {
        List<Range> ranges = new ArrayList<>();

        NodeVisitor visitor = new NodeVisitor(
                new VisitHandler<>(Link.class, n -> addRange(n, blockStartOffset, ranges)),
                new VisitHandler<>(Image.class, n -> addRange(n, blockStartOffset, ranges)),
                new VisitHandler<>(Code.class, n -> addRange(n, blockStartOffset, ranges)),
                new VisitHandler<>(AutoLink.class, n -> addRange(n, blockStartOffset, ranges)),
                new VisitHandler<>(HtmlInline.class, n -> addRange(n, blockStartOffset, ranges))
        );

        visitor.visit(root);
        ranges.sort(Comparator.comparingInt(r -> r.start));
        return mergeRanges(ranges);
    }

    private static void addRange(Node node, int blockStart, List<Range> ranges) {
        int s = Math.max(0, node.getStartOffset() - blockStart);
        int e = Math.max(0, node.getEndOffset() - blockStart);
        if (e > s) ranges.add(new Range(s, e));
    }

    private static List<Range> mergeRanges(List<Range> input) {
        if (input.isEmpty()) return input;
        List<Range> out = new ArrayList<>();
        Range cur = input.get(0);

        for (int i = 1; i < input.size(); i++) {
            Range r = input.get(i);
            if (r.start <= cur.end) {
                cur = new Range(cur.start, Math.max(cur.end, r.end));
            } else {
                out.add(cur);
                cur = r;
            }
        }
        out.add(cur);
        return out;
    }

    private static boolean inProtected(int pos, List<Range> ranges) {
        for (Range r : ranges) {
            if (pos >= r.start && pos < r.end) return true;
        }
        return false;
    }

    /* ===================== 句子级切分 ===================== */

    public static List<Range> splitBySentenceBoundaries(
            String text,
            List<Range> protectedRanges
    ) {
        List<Range> ranges = new ArrayList<>();
        int n = text.length();
        int last = 0;

        for (int i = 0; i < n; i++) {
            if (inProtected(i, protectedRanges)) continue;

            char c = text.charAt(i);

            if (c == '\n') {
                int j = i;
                while (j < n && text.charAt(j) == '\n') j++;
                int k = j;
                while (k < n && Character.isWhitespace(text.charAt(k)) && text.charAt(k) != '\n') k++;
                if (k < n && text.charAt(k) == '\n') {
                    int cut = i + 1;
                    if (cut > last) ranges.add(new Range(last, cut));
                    last = cut;
                    continue;
                }
            }

            if (SENTENCE_END.indexOf(c) >= 0) {
                int cut = i + 1;
                if (cut > last) ranges.add(new Range(last, cut));
                last = cut;
            }
        }

        if (last < n) ranges.add(new Range(last, n));

        List<Range> filtered = new ArrayList<>();
        for (Range r : ranges) {
            if (!text.substring(r.start, r.end).trim().isEmpty()) {
                filtered.add(r);
            }
        }
        return filtered;
    }

    /* ===================== 兜底强切（保留 block） ===================== */

    public static List<TextChunk> forceSplitIfStillTooLong(
            List<TextChunk> chunks,
            int maxChars
    ) {
        List<TextChunk> out = new ArrayList<>();
        int idx = 0;

        for (TextChunk c : chunks) {
            String s = c.getContent();
            if (s.length() <= maxChars) {
                out.add(c);
                continue;
            }

            int start = 0;
            while (start < s.length()) {
                int end = Math.min(start + maxChars, s.length());
                int cut = findLastWhitespace(s, start, end);
                if (cut <= start + 20) cut = end;

                String part = s.substring(start, cut).trim();
                if (!part.isEmpty()) {
                    out.add(new TextChunk(
                            idx++,
                            c.getStart(),      // 近似
                            c.getEnd(),        // 近似
                            c.getBlockStart(), // 保留 block
                            c.getBlockEnd(),
                            part
                    ));
                }
                start = cut;
            }
        }
        return out;
    }

    private static int findLastWhitespace(String s, int start, int end) {
        for (int i = end - 1; i > start; i--) {
            if (Character.isWhitespace(s.charAt(i))) return i;
        }
        return -1;
    }

    /* ===================== Range ===================== */

    public static class Range {
        final int start;
        final int end;
        Range(int s, int e) {
            start = s;
            end = e;
        }
    }
}