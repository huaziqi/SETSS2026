package com.LHZ.SETSS2026.controller;

import com.LHZ.SETSS2026.common.result.Result;
import com.LHZ.SETSS2026.dto.review.AssignManuscriptRequest;
import com.LHZ.SETSS2026.entity.Manuscript;
import com.LHZ.SETSS2026.service.ManuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("api/manuscript")
@RequiredArgsConstructor
public class ManuController {
    private final ManuService manuService;

    //提交新稿件
    //提交新稿件（带文件上传）
    @PostMapping("/submit")
    public Result submitManuscript(
            @RequestParam("name") String name,
            @RequestParam("id") Integer id,
            @RequestParam("title") String title,
            @RequestParam("introduction") String introduction,
            @RequestParam("file") MultipartFile file
    ){
        try{
            Manuscript manuscript = new Manuscript();
            manuscript.setAuthor(name);
            manuscript.setUserId(id);
            manuscript.setTitle(title);
            manuscript.setIntroduction(introduction);

            Manuscript saved = manuService.submitManuscript(manuscript, file);
            return Result.success(saved);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("提交稿件失败：" + e.getMessage());
        }
    }

    //更新稿件
    @PostMapping("/update/{manuId}")
    public  Result updateManuscript(@PathVariable Integer manuId,@RequestBody Manuscript manuscript) {
        try {
            Manuscript updated = manuService.updateManuscript(manuId, manuscript);
            return Result.success(updated);

        } catch (Exception e) {
            return Result.error("更新失败：" + e.getMessage());
        }
    }

        //删除稿件
        @DeleteMapping("/delete/{manuId}")
        public Result deleteManuscript(@PathVariable Integer manuId) {
            try {
                manuService.deleteManuscript(manuId);
                return Result.success("删除成功");
            } catch (Exception e) {
                return Result.error("删除失败：" + e.getMessage());
            }
        }

        //分配稿件
        @PostMapping("/assign")
        public Result assignManuscript(@RequestBody AssignManuscriptRequest request) {
            try {
                Manuscript manuscript = manuService.assignManuscript(
                        request.getManuscriptId(),
                        request.getReviewerId()
                );
                return Result.success("稿件分配成功", manuscript);
            } catch (Exception e) {
                return Result.error("稿件分配失败：" + e.getMessage());
            }
        }

    //查询所有稿件
        @GetMapping("/list")
        public Result listManuscripts() {
            try {
                List<Manuscript> list = manuService.getAllManuscripts();
                return Result.success(list);
            } catch (Exception e) {
                return Result.error("查询失败：" + e.getMessage());
            }
        }

        //根据状态查询稿件
        @GetMapping("/status/{status}")
        public Result getManuscriptsByStatus(@PathVariable String status) {
            try {
                List<Manuscript> list = manuService.getManuscriptsByStatus(status);
                return Result.success(list);
            } catch (Exception e) {
                return Result.error("查询失败：" + e.getMessage());
            }
        }

        //根据审稿人查询稿件
        @GetMapping("/status/{status}")
        public Result getManuscriptsByReviewer(@PathVariable String reviewer) {
        try {
            List<Manuscript> list = manuService.getManuscriptsByReviewer(reviewer);
            return Result.success(list);
        }catch (Exception e){
            return Result.error("查询失败：" + e.getMessage());
            }
        }

        //根据作者查询稿件
        @GetMapping("/author/{author}")
        public  Result getManuscriptsByAuthor(@PathVariable String author){
            try{
                List<Manuscript> list = manuService.getManuscriptsByAuthor(author);
                return Result.success(list);
            }catch (Exception e){
                return Result.error("查询失败：" + e.getMessage());
            }
        }

        //根据ID查询稿件
        @GetMapping("/{manuId}")
        public Result getManuscriptById(@PathVariable Integer manuId) {
            try {
                return manuService.getManuscriptById(manuId)
                        .map(Result::success)
                        .orElse(Result.error("稿件不存在"));

            } catch (Exception e) {
                return Result.error("查询失败：" + e.getMessage());
            }
        }

}

