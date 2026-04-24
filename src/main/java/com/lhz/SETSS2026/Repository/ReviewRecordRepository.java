package com.LHZ.SETSS2026.repository;

import com.LHZ.SETSS2026.entity.ReviewRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRecordRepository extends JpaRepository<ReviewRecord, Integer> {
    List<ReviewRecord> findByManuscriptId(Integer manuscriptId);
    List<ReviewRecord> findByReviewerId(Integer reviewerId);
    List<ReviewRecord> findByManuscriptIdAndReviewerId(Integer manuscriptId, Integer reviewerId);
}
