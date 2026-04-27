package com.LHZ.SETSS2026.service;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import org.springframework.stereotype.Service;

@Service
public class MarkdownRenderService {

    private final Parser parser;
    private final HtmlRenderer renderer;

    public MarkdownRenderService() {
        this.parser = Parser.builder().build();
        this.renderer = HtmlRenderer.builder().build();
    }

    public String renderWithBlockAttrs(String markdown) {
        if (markdown == null || markdown.isBlank()) {
            return "";
        }

        Node doc = parser.parse(markdown);
        StringBuilder html = new StringBuilder();

        int blockIndex = 0;

        for (Node block = doc.getFirstChild(); block != null; block = block.getNext()) {
            int start = block.getStartOffset();
            int end = block.getEndOffset();

            String blockHtml = renderer.render(block);

            html.append("<div class=\"md-block\"")
                    .append(" data-md-block=\"").append(blockIndex).append("\"")
                    .append(" data-md-start=\"").append(start).append("\"")
                    .append(" data-md-end=\"").append(end).append("\"")
                    .append(">")
                    .append(blockHtml)
                    .append("</div>");

            blockIndex++;
        }

        return html.toString();
    }
    public String renderWithSearchAnchor(String markdown, String anchorId, Integer blockStart, Integer blockEnd) {
        if (markdown == null || markdown.isBlank()) {
            return "";
        }

        Node doc = parser.parse(markdown);
        StringBuilder html = new StringBuilder();

        int blockIndex = 0;

        for (Node block = doc.getFirstChild(); block != null; block = block.getNext()) {
            int start = block.getStartOffset();
            int end = block.getEndOffset();

            String blockHtml = renderer.render(block);

            boolean inMatchedChunk =
                    anchorId != null &&
                            blockStart != null &&
                            blockEnd != null &&
                            blockIndex >= blockStart &&
                            blockIndex <= blockEnd;

            html.append("<div");

            if (inMatchedChunk && blockIndex == blockStart) {
                html.append(" id=\"").append(anchorId).append("\"");
            }

            html.append(" class=\"md-block")
                    .append(inMatchedChunk ? " matched-chunk" : "")
                    .append("\"")
                    .append(" data-md-block=\"").append(blockIndex).append("\"")
                    .append(" data-md-start=\"").append(start).append("\"")
                    .append(" data-md-end=\"").append(end).append("\"")
                    .append(">")
                    .append(blockHtml)
                    .append("</div>");

            blockIndex++;

        }

        return html.toString();
    }
}