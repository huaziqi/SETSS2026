package com.LHZ.SETSS2026.dto;

import com.LHZ.SETSS2026.entity.ConferencePage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConferencePageDTO {

    private Long pageId;
    private String pageKey;
    private String title;
    private String content;
    private String status;
    private LocalDateTime publishTime;
    private LocalDateTime updateTime;
    private String htmlContent;

    public static ConferencePageDTO fromEntity(ConferencePage page) {
        ConferencePageDTO dto = new ConferencePageDTO();

        dto.setPageId(page.getPageId());
        dto.setPageKey(page.getPageKey());
        dto.setTitle(page.getTitle());
        dto.setContent(page.getContent());
        dto.setStatus(page.getStatus());
        dto.setPublishTime(page.getPublishTime());
        dto.setUpdateTime(page.getUpdateTime());

        return dto;
    }

    public void updateEntity(ConferencePage page) {
        if (this.title != null) {
            page.setTitle(this.title);
        }

        if (this.content != null) {
            page.setContent(this.content);
        }

        if (this.status != null) {
            page.setStatus(this.status);
        }
    }
}