package com.LHZ.SETSS2026.service;

import com.LHZ.SETSS2026.entity.Manuscript;
import com.LHZ.SETSS2026.entity.ReviewRecord;
import com.LHZ.SETSS2026.repository.ManuRepository;
import com.LHZ.SETSS2026.repository.ReviewRecordRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class ReviewService {
    private  final ReviewRecordRepository reviewRecordRepository;
    private  final ManuRepository manuRepository;
    // 提交审稿意见
    @Transactional
    public ReviewRecord submitReview(Integer manuscriptId, Integer reviewerId,
                                     String reviewResult, String reviewComment) {
        // 验证稿件是否存在
        Manuscript manuscript = manuRepository.findById(manuscriptId)
                .orElseThrow(() -> new RuntimeException("稿件不存在"));

        // 创建审稿记录
        ReviewRecord record = new ReviewRecord();
        record.setManuscriptId(manuscriptId);
        record.setReviewerId(reviewerId);
        record.setReviewTime(LocalDateTime.now());
        record.setReviewResult(reviewResult);
        record.setReviewComment(reviewComment);


        manuscript.setReviewerId(reviewerId);
        manuscript.setReviewResult(reviewResult);
        manuscript.setUpdateTime(LocalDateTime.now());
        manuRepository.save(manuscript);

        return reviewRecordRepository.save(record);
    }

    // 查询某稿件的所有审稿记录
    public List<ReviewRecord> getReviewsByManuscriptId(Integer manuscriptId) {
        return reviewRecordRepository.findByManuscriptId(manuscriptId);
    }

    // 查询某审稿员的所有审稿记录
    public List<ReviewRecord> getReviewsByReviewerId(Integer reviewerId) {
        return reviewRecordRepository.findByReviewerId(reviewerId);
    }

    // 查询某审稿员对某稿件的审稿记录
    public List<ReviewRecord> getReviewsByManuscriptAndReviewer(Integer manuscriptId, Integer reviewerId) {
        return reviewRecordRepository.findByManuscriptIdAndReviewerId(manuscriptId, reviewerId);
    }

    // 通过审稿（简化方法）
    @Transactional
    public ReviewRecord approveManuscript(Integer manuscriptId, Integer reviewerId, String comment) {
        return submitReview(manuscriptId, reviewerId, "通过", comment);
    }

    // 驳回稿件（简化方法）
    @Transactional
    public ReviewRecord rejectManuscript(Integer manuscriptId, Integer reviewerId, String comment) {
        return submitReview(manuscriptId, reviewerId, "驳回", comment);
    }
}
