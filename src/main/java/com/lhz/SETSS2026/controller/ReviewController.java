package com.LHZ.SETSS2026.controller;

import com.LHZ.SETSS2026.common.result.Result;
import com.LHZ.SETSS2026.entity.ReviewRecord;
import com.LHZ.SETSS2026.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/review")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    // 提交审稿意见
    @PostMapping("/submit")
    public Result submitReview(@RequestBody Map<String,  Object> request) {
        try{
            Integer manuscriptId = (Integer) request.get("manuscriptId");
            Integer reviewerId = (Integer) request.get("reviewerId");
            String reviewResult = (String)request.get("reviewResult");
            String reviewComment = (String)request.get("reviewComment");

            ReviewRecord record = reviewService.submitReview(manuscriptId, reviewerId, reviewResult, reviewComment);
            return Result.success(record);
        }catch (Exception e){
            return Result.error("提交审稿意见失败" + e.getMessage());
        }
    }

    //稿件通过
    @PostMapping("/approve")
    public Result approveManuscript(@RequestBody Map<String,  Object> request) {
        try{
            Integer manuscriptId = (Integer) request.get("manuscriptId");
            Integer reviewerId = (Integer) request.get("reviewerId");
            String comment = (String)request.get("comment");

            ReviewRecord record = reviewService.approveManuscript(manuscriptId, reviewerId, comment);
            return Result.success("审核通过", record);

        }catch (Exception e){
            return Result.error("稿件审核失败" + e.getMessage());
        }
    }

    //稿件驳回
    @PostMapping("/reject")
    public Result rejectManuscript(@RequestBody Map<String,  Object> request) {
        try{
            Integer manuscriptId = (Integer) request.get("manuscriptId");
            Integer reviewerId = (Integer) request.get("reviewerId");
            String comment = (String)request.get("comment");

            ReviewRecord record = reviewService.rejectManuscript(manuscriptId, reviewerId, comment);
            return Result.success("已驳回", record);
        }catch (Exception e){
            return Result.error("驳回失败" + e.getMessage());
        }
    }

    // 查询某稿件的所有审稿记录
    @GetMapping("/manuscript/{manuscriptId}")
    public Result getReviewsByManuscriptId(@PathVariable Integer manuscriptId) {
        try{
            return Result.success(reviewService.getReviewsByManuscriptId(manuscriptId));
        }catch (Exception e){
            return Result.error("查询失败" + e.getMessage());
        }
    }

    // 查询某审稿员所有的审稿记录
    @GetMapping("/reviewer/{reviewerId}")
    public Result getReviewsByReviewerId(@PathVariable Integer reviewerId) {
        try{
            return Result.success(reviewService.getReviewsByReviewerId(reviewerId));
        }catch (Exception e){
            return Result.error("查询失败" + e.getMessage());
        }
    }
}

