package com.LHZ.SETSS2026.service;

import com.LHZ.SETSS2026.dto.semantic.TextChunk;
import com.LHZ.SETSS2026.entity.ConferencePage;
import com.LHZ.SETSS2026.entity.Post;
import com.LHZ.SETSS2026.repository.ConferencePageRepository;
import com.LHZ.SETSS2026.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticleEmbeddingService {

    private final PostRepository postRepository;
    private final ConferencePageRepository conferencePageRepository;

    @Qualifier("postgresJdbcTemplate")
    private final JdbcTemplate pgJdbcTemplate;

    @Qualifier("embedding")
    private final WebClient embeddingClient;

    private final SemanticMarkdownChunkService semanticMarkdownChunkService;

    @Value("${chat.bailian.key}")
    private String bailianKey;

    @Transactional
    public void syncAllPostEmbeddings() {
        List<Post> posts = postRepository.findAll();

        for (Post post : posts) {
            if (!"PUBLISHED".equalsIgnoreCase(post.getStatus())) {
                deleteEmbeddings("POST", post.getPostId());
                continue;
            }

            syncPostEmbeddings(post);
        }
    }

    @Transactional
    public void syncAllConferencePageEmbeddings() {
        List<ConferencePage> pages = conferencePageRepository.findAll();

        for (ConferencePage page : pages) {
            if (!"PUBLISHED".equalsIgnoreCase(page.getStatus())) {
                deleteEmbeddings("CONFERENCE_PAGE", page.getPageId());
                continue;
            }

            syncConferencePageEmbeddings(page);
        }
    }

    @Transactional
    public void syncPostEmbeddings(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        syncPostEmbeddings(post);
    }

    @Transactional
    public void syncConferencePageEmbeddings(String pageKey) {
        ConferencePage page = conferencePageRepository.findByPageKey(pageKey)
                .orElseThrow(() -> new RuntimeException("Conference page not found"));

        syncConferencePageEmbeddings(page);
    }

    @Transactional
    public void deleteEmbeddings(String sourceType, Long sourceId) {
        int rows = pgJdbcTemplate.update(
                "DELETE FROM semantic_chunks WHERE source_type = ? AND source_id = ?",
                sourceType,
                sourceId
        );

        System.out.println("Deleted embeddings: " + sourceType + " " + sourceId + ", rows=" + rows);
    }

    private void syncPostEmbeddings(Post post) {
        String markdown = buildSearchMarkdown(post.getTitle(), post.getContent());

        syncMarkdownEmbeddings(
                "POST",
                post.getPostId(),
                null,
                post.getTitle(),
                "/forum/post/" + post.getPostId(),
                markdown
        );
    }

    private void syncConferencePageEmbeddings(ConferencePage page) {
        String markdown = buildSearchMarkdown(page.getTitle(), page.getContent());

        syncMarkdownEmbeddings(
                "CONFERENCE_PAGE",
                page.getPageId(),
                page.getPageKey(),
                page.getTitle(),
                "/" + page.getPageKey(),
                markdown
        );
    }

    private void syncMarkdownEmbeddings(
            String sourceType,
            Long sourceId,
            String pageKey,
            String title,
            String url,
            String markdown
    ) {
        deleteEmbeddings(sourceType, sourceId);

        if (markdown == null || markdown.isBlank()) {
            return;
        }

        List<TextChunk> chunks = semanticMarkdownChunkService.splitMarkdown(markdown);

        for (TextChunk chunk : chunks) {
            if (chunk.getContent() == null || chunk.getContent().isBlank()) {
                continue;
            }

            List<Double> embedding = fetchEmbedding(chunk.getContent());

            if (embedding == null || embedding.isEmpty()) {
                continue;
            }

            String vectorLiteral = toVectorLiteral(embedding);
            String anchorId = buildAnchorId(sourceType, sourceId, chunk.getIndex());

            pgJdbcTemplate.update(
                    """
                    INSERT INTO semantic_chunks
                    (
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
                        embedding
                    )
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::vector)
                    ON CONFLICT (source_type, source_id, chunk_index)
                    DO UPDATE SET
                        page_key = EXCLUDED.page_key,
                        title = EXCLUDED.title,
                        url = EXCLUDED.url,
                        anchor_id = EXCLUDED.anchor_id,
                        char_start = EXCLUDED.char_start,
                        char_end = EXCLUDED.char_end,
                        block_start = EXCLUDED.block_start,
                        block_end = EXCLUDED.block_end,
                        content = EXCLUDED.content,
                        embedding = EXCLUDED.embedding
                    """,
                    sourceType,
                    sourceId,
                    pageKey,
                    title,
                    url,
                    chunk.getIndex(),
                    anchorId,
                    chunk.getStart(),
                    chunk.getEnd(),
                    chunk.getBlockStart(),
                    chunk.getBlockEnd(),
                    chunk.getContent(),
                    vectorLiteral
            );
        }
    }

    private String buildSearchMarkdown(String title, String content) {
        String safeTitle = title == null ? "" : title.trim();
        String safeContent = content == null ? "" : content.trim();

        if (safeTitle.isBlank()) {
            return safeContent;
        }

        return "# " + safeTitle + "\n\n" + safeContent;
    }

    private String buildAnchorId(String sourceType, Long sourceId, Integer chunkIndex) {
        return sourceType.toLowerCase(Locale.ROOT)
                .replace("_", "-")
                + "-"
                + sourceId
                + "-chunk-"
                + chunkIndex;
    }

    public List<Double> fetchEmbedding(String text) {
        Map<String, Object> requestBody = Map.of(
                "model", "qwen3-vl-embedding",
                "input", Map.of(
                        "contents", List.of(
                                Map.of("text", text)
                        )
                ),
                "parameters", Map.of(
                        "enable_fusion", true,
                        "dimension", 1024
                )
        );

        Map response = embeddingClient.post()
                .uri("https://dashscope.aliyuncs.com/api/v1/services/embeddings/multimodal-embedding/multimodal-embedding")
                .header("Authorization", "Bearer " + bailianKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response == null || !response.containsKey("output")) {
            return null;
        }

        Map output = (Map) response.get("output");
        List<Map> embeddings = (List<Map>) output.get("embeddings");

        if (embeddings == null || embeddings.isEmpty()) {
            return null;
        }

        List<Number> vector = (List<Number>) embeddings.get(0).get("embedding");

        return vector.stream()
                .map(Number::doubleValue)
                .toList();
    }

    private String toVectorLiteral(List<Double> embedding) {
        return embedding.stream()
                .filter(Objects::nonNull)
                .map(d -> String.format(Locale.ROOT, "%f", d))
                .collect(Collectors.joining(",", "[", "]"));
    }
}