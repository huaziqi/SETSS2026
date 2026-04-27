package com.LHZ.SETSS2026.service;

import com.LHZ.SETSS2026.dto.semantic.SemanticSearchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SemanticSearchService {

    @Qualifier("postgresJdbcTemplate")
    private final JdbcTemplate pgJdbcTemplate;

    private final ArticleEmbeddingService articleEmbeddingService;

    public List<SemanticSearchResult> search(String keyword, Integer limit) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }

        int safeLimit = limit == null ? 5 : Math.min(Math.max(limit, 1), 20);

        List<Double> embedding = articleEmbeddingService.fetchEmbedding(keyword);

        if (embedding == null || embedding.isEmpty()) {
            return List.of();
        }

        String vectorLiteral = toVectorLiteral(embedding);

        String sql = """
                SELECT
                    source_type,
                    source_id,
                    page_key,
                    title,
                    url,
                    chunk_index,
                    anchor_id,
                    char_start,
                    char_end,
                    block_start,
                    block_end,
                    content,
                    embedding <=> ?::vector AS distance,
                    1 - (embedding <=> ?::vector) AS similarity
                FROM semantic_chunks
                ORDER BY embedding <=> ?::vector
                LIMIT ?
                """;

        return pgJdbcTemplate.query(
                sql,
                (rs, rowNum) -> new SemanticSearchResult(
                        rs.getString("source_type"),
                        rs.getLong("source_id"),
                        rs.getString("page_key"),
                        rs.getString("title"),
                        rs.getString("url"),
                        rs.getInt("chunk_index"),
                        rs.getString("anchor_id"),
                        rs.getInt("char_start"),
                        rs.getInt("char_end"),
                        rs.getInt("block_start"),
                        rs.getInt("block_end"),
                        rs.getString("content"),
                        rs.getDouble("distance"),
                        rs.getDouble("similarity")
                ),
                vectorLiteral,
                vectorLiteral,
                vectorLiteral,
                safeLimit
        );
    }

    private String toVectorLiteral(List<Double> embedding) {
        return embedding.stream()
                .filter(Objects::nonNull)
                .map(d -> String.format(Locale.ROOT, "%f", d))
                .collect(Collectors.joining(",", "[", "]"));
    }
}