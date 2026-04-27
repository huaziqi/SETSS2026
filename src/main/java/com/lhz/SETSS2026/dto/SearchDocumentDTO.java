package com.LHZ.SETSS2026.dto;

import com.LHZ.SETSS2026.entity.ConferencePage;
import com.LHZ.SETSS2026.entity.Post;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchDocumentDTO {

    private String sourceType;
    private Long sourceId;
    private String pageKey;
    private String title;
    private String content;
    private String status;
    private String tag;
    private String url;
    private LocalDateTime updateTime;

    public static SearchDocumentDTO fromPost(Post post) {
        SearchDocumentDTO dto = new SearchDocumentDTO();

        dto.setSourceType("POST");
        dto.setSourceId(post.getPostId());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setStatus(post.getStatus());
        dto.setTag(post.getTag());
        dto.setUrl("/posts/" + post.getPostId());
        dto.setUpdateTime(post.getUpdateTime());

        return dto;
    }

    public static SearchDocumentDTO fromConferencePage(ConferencePage page) {
        SearchDocumentDTO dto = new SearchDocumentDTO();

        dto.setSourceType("CONFERENCE_PAGE");
        dto.setSourceId(page.getPageId());
        dto.setPageKey(page.getPageKey());
        dto.setTitle(page.getTitle());
        dto.setContent(page.getContent());
        dto.setStatus(page.getStatus());
        dto.setTag("conference");
        dto.setUrl("/" + page.getPageKey());
        dto.setUpdateTime(page.getUpdateTime());

        return dto;
    }
}