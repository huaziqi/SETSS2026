package com.LHZ.SETSS2026.repository;

import com.LHZ.SETSS2026.entity.LectureModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LectureModuleRepository extends JpaRepository<LectureModule, Integer> {

    List<LectureModule> findAllByOrderByModuleNoAsc();
}
