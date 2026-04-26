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

    private final ConferencePageRepository conferencePageRepository;

    public List<ConferencePageDTO> listPages() {
        return conferencePageRepository.findAll()
                .stream()
                .map(ConferencePageDTO::fromEntity)
                .toList();
    }

    public ConferencePageDTO getPageByKey(String pageKey) {
        ConferencePage page = conferencePageRepository.findByPageKey(pageKey)
                .orElseThrow(() -> new EntityNotFoundException("Conference page not found"));

        return ConferencePageDTO.fromEntity(page);
    }

    @Transactional
    public ConferencePageDTO updatePage(String pageKey, ConferencePageDTO dto) {
        ConferencePage page = conferencePageRepository.findByPageKey(pageKey)
                .orElseThrow(() -> new EntityNotFoundException("Conference page not found"));

        dto.updateEntity(page);

        ConferencePage saved = conferencePageRepository.save(page);

        return ConferencePageDTO.fromEntity(saved);
    }
}