package com.LHZ.SETSS2026.controller;

import com.LHZ.SETSS2026.dto.participaton.LectureModuleDTO;
import com.LHZ.SETSS2026.service.ParticipationService;
import com.LHZ.SETSS2026.service.ParticipationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/participation")
public class ParticipationController {

    @Autowired
    private ParticipationService participationService;

    /**
     * 获取所有讲座模块列表
     */
    @GetMapping("/lecture-modules")
    public ResponseEntity<List<LectureModuleDTO>> getLectureModules() {
        List<LectureModuleDTO> modules = participationService.getAllLectureModules();
        return ResponseEntity.ok(modules);
    }
}
