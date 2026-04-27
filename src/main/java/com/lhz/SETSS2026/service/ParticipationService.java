package com.LHZ.SETSS2026.service;

import com.LHZ.SETSS2026.dto.participaton.LectureModuleDTO;
import com.LHZ.SETSS2026.entity.LectureModule;
import com.LHZ.SETSS2026.repository.LectureModuleRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ParticipationService {

    @Autowired
    private LectureModuleRepository lectureModuleRepository;

    @PostConstruct
    public void initData() {
        if (lectureModuleRepository.count() == 0) {
            try {
                String content = Files.readString(Paths.get("src/main/resources/lectureModule.json"));
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                List<LectureModule> modules = mapper.readValue(content,
                        mapper.getTypeFactory().constructCollectionType(List.class, LectureModule.class));

                modules.forEach(module -> module.setId(null));

                lectureModuleRepository.saveAll(modules);
            } catch (IOException e) {
                throw new RuntimeException("Failed to load lecture modules data", e);
            }
        }
    }

    public List<LectureModuleDTO> getAllLectureModules() {
        List<LectureModule> modules = lectureModuleRepository.findAllByOrderByModuleNoAsc();
        return modules.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private LectureModuleDTO convertToDTO(LectureModule module) {
        LectureModuleDTO dto = new LectureModuleDTO();
        dto.setId(module.getId());
        dto.setModuleNo(module.getModuleNo());
        dto.setLecturer(module.getLecturer());
        dto.setCourseTopic(module.getCourseTopic());
        dto.setIntroduction(module.getIntroduction());
        dto.setExactTime(module.getExactTime());
        dto.setPrice(module.getPrice());
        dto.setSelected(false);
        return dto;
    }
}

