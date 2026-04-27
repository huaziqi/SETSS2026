package com.LHZ.SETSS2026.service;

import com.LHZ.SETSS2026.dto.ConferencePageDTO;
import com.LHZ.SETSS2026.entity.ConferencePage;
import com.LHZ.SETSS2026.repository.ConferencePageRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConferencePageService {

    private final MarkdownRenderService markdownRenderService;
    private final ConferencePageRepository conferencePageRepository;
    private final ArticleEmbeddingService articleEmbeddingService;

    public List<ConferencePageDTO> listPages() {
        return conferencePageRepository.findAll()
                .stream()
                .map(ConferencePageDTO::fromEntity)
                .toList();
    }

    public ConferencePageDTO getPageByKey(String pageKey) {
        ConferencePage page = conferencePageRepository.findByPageKey(pageKey)
                .orElseThrow(() -> new EntityNotFoundException("Conference page not found"));

        ConferencePageDTO dto = ConferencePageDTO.fromEntity(page);

        dto.setHtmlContent(
                markdownRenderService.render(page.getContent())
        );

        return dto;
    }

    @Transactional
    public ConferencePageDTO updatePage(String pageKey, ConferencePageDTO dto) {
        ConferencePage page = conferencePageRepository.findByPageKey(pageKey)
                .orElseThrow(() -> new EntityNotFoundException("Conference page not found"));

        dto.updateEntity(page);

        ConferencePage saved = conferencePageRepository.save(page);

        articleEmbeddingService.syncConferencePageEmbeddings(saved.getPageKey());

        ConferencePageDTO result = ConferencePageDTO.fromEntity(saved);
        result.setHtmlContent(
                markdownRenderService.render(saved.getContent())
        );

        return result;
    }
}