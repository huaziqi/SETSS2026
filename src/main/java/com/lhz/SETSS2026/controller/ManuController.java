package com.LHZ.SETSS2026.controller;

import com.LHZ.SETSS2026.common.result.Result;
import com.LHZ.SETSS2026.dto.Manuscripts.ManuscriptSimpleDTO;
import com.LHZ.SETSS2026.dto.review.AssignManuscriptRequest;
import com.LHZ.SETSS2026.entity.Manuscript;
import com.LHZ.SETSS2026.service.ManuService;
import jakarta.servlet.http.HttpServletResponse;
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


    @GetMapping("/user/{userId}")
    public Result getManuscriptsByUserId(@PathVariable Integer userId) {
        try {
            List<ManuscriptSimpleDTO> list = manuService.getManuscriptsByUserId(userId);
            return Result.success(list);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
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


    @GetMapping("/update/{manuId}")
    public Result getManuscriptForUpdate(@PathVariable Integer manuId) {
        try {
            ManuscriptSimpleDTO dto = manuService.getManuscriptForUpdate(manuId);
            return Result.success(dto);
        } catch (Exception e) {
            return Result.error("获取稿件信息失败：" + e.getMessage());
        }
    }


    @GetMapping("/update/download/{manuId}")
    public void downloadManuscriptFile(@PathVariable Integer manuId, HttpServletResponse response) {
        try {
            manuService.downloadManuscriptFile(manuId, response);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try {
                response.getWriter().write("下载失败：" + e.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }


    @PostMapping("/update/submit/{manuId}")
    public Result updateManuscriptSubmit(
            @PathVariable Integer manuId,
            @RequestParam("name") String name,
            @RequestParam("id") Integer id,
            @RequestParam("title") String title,
            @RequestParam("introduction") String introduction,
            @RequestParam(value = "file", required = false) MultipartFile file
    ) {
        try {
            Manuscript manuscript = new Manuscript();
            manuscript.setAuthor(name);
            manuscript.setUserId(id);
            manuscript.setTitle(title);
            manuscript.setIntroduction(introduction);

            Manuscript updated = manuService.updateManuscriptSubmit(manuId, manuscript, file);
            return Result.success(updated);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("更新稿件失败：" + e.getMessage());
        }
    }






        //查询所有稿件
    @GetMapping("/list")
    public Result listManuscripts() {
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
        @GetMapping("/reviewer/{reviewer}")
        public Result getManuscriptsByReviewer(@PathVariable String reviewer) {
        try {
            List<Manuscript> list = manuService.getAllManuscripts();
            return Result.success(list);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }



    //根据状态查询稿件
    public Result getManuscriptsByStatus(@PathVariable String status) {
        try {
            List<Manuscript> list = manuService.getManuscriptsByStatus(status);
            return Result.success(list);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    //根据审稿人查询稿件
    public Result getManuscriptsByReviewer(@PathVariable String reviewer) {
    try {
        List<Manuscript> list = manuService.getManuscriptsByReviewer(reviewer);
        return Result.success(list);
    }catch (Exception e){
        return Result.error("查询失败：" + e.getMessage());
        }
    }

    //根据作者查询稿件
    public  Result getManuscriptsByAuthor(@PathVariable String author){
        try{
            List<Manuscript> list = manuService.getManuscriptsByAuthor(author);
            return Result.success(list);
        }catch (Exception e){
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    //根据ID查询稿件
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

