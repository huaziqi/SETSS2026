package com.LHZ.SETSS2026.controller;

import com.LHZ.SETSS2026.dto.semantic.SemanticSearchRequest;
import com.LHZ.SETSS2026.dto.semantic.SemanticSearchResult;
import com.LHZ.SETSS2026.service.SemanticSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/semantic")
@RequiredArgsConstructor
public class SemanticSearchController {

    private final SemanticSearchService semanticSearchService;

    @PostMapping("/search")
    public List<SemanticSearchResult> search(@RequestBody SemanticSearchRequest request) {
        return semanticSearchService.search(
                request.getKeyword(),
                request.getLimit()
        );
    }
}