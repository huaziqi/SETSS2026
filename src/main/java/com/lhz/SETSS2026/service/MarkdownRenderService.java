package com.LHZ.SETSS2026.service;

import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MarkdownRenderService {

    private final Parser parser;
    private final HtmlRenderer renderer;

    public MarkdownRenderService() {
        MutableDataSet options = new MutableDataSet();

        options.set(Parser.EXTENSIONS, List.of(
                TablesExtension.create()
        ));

        this.parser = Parser.builder(options).build();
        this.renderer = HtmlRenderer.builder(options).build();
    }

    public String render(String markdown) {
        if (markdown == null || markdown.isBlank()) {
            return "";
        }

        return renderer.render(parser.parse(markdown));
    }
}