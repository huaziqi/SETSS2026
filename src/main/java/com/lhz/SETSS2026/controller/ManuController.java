package com.LHZ.SETSS2026.controller;

import com.LHZ.SETSS2026.common.result.Result;
import com.LHZ.SETSS2026.dto.Assign.UserSimpleDTO;
import com.LHZ.SETSS2026.dto.Manuscripts.ManuscriptReviewDTO;
import com.LHZ.SETSS2026.dto.Manuscripts.ManuscriptSimpleDTO;
import com.LHZ.SETSS2026.entity.Manuscript;
import com.LHZ.SETSS2026.enums.ManuscriptStatus;
import com.LHZ.SETSS2026.service.ManuService;
import com.LHZ.SETSS2026.service.UserService;
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
    private final UserService userService;

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


    @GetMapping("/admin/check")
    public Result getManuscriptsForChecking() {
        try {
            List<ManuscriptSimpleDTO> list = manuService.getManuscriptsByStatus("AwaitingChecking");
            return Result.success(list);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/admin/check-single/{manuId}")
    public Result getManuscriptForChecking(@PathVariable Integer manuId) {
        try {
            ManuscriptSimpleDTO dto = manuService.getManuscriptForChecking(manuId);
            return Result.success(dto);
        } catch (Exception e) {
            return Result.error("获取稿件信息失败：" + e.getMessage());
        }
    }

    //管理员审核时下载稿件源文件
    @GetMapping("/admin/check-download/{manuId}")
    public void downloadManuscriptForCheck(@PathVariable Integer manuId, HttpServletResponse response) {
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

    @PostMapping("/admin/approve/{manuId}")
    public Result approveManuscript(@PathVariable Integer manuId) {
        try {
            manuService.updateManuscriptStatus(manuId, ManuscriptStatus.AwaitingAssigning);
            return Result.success("稿件审核通过");
        } catch (Exception e) {
            return Result.error("稿件审核失败：" + e.getMessage());
        }
    }


    @PostMapping("/admin/reject/{manuId}")
    public Result rejectManuscript(@PathVariable Integer manuId, @RequestParam String reason) {
        try {
            if (reason == null || reason.trim().isEmpty()) {
                return Result.error("拒绝原因不能为空");
            }

            manuService.rejectManuscriptWithReason(manuId, reason.trim());
            ManuscriptSimpleDTO dto = manuService.getManuscriptForUpdate(manuId);
            return Result.success("稿件审核未通过", dto);
        } catch (Exception e) {
            return Result.error("稿件审核失败：" + e.getMessage());
        }
    }


    @GetMapping("/get-single/{manuId}")
    public Result getSingleManuscript(@PathVariable Integer manuId) {
        try {
            ManuscriptSimpleDTO dto = manuService.getManuscriptForChecking(manuId);
            return Result.success(dto);
        } catch (Exception e) {
            return Result.error("获取稿件信息失败：" + e.getMessage());
        }
    }

    //管理员审核时下载稿件源文件
    @GetMapping("/download/{manuId}")
    public void downloadManuscript(@PathVariable Integer manuId, HttpServletResponse response) {
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


    @GetMapping("/chair/assign/manu")
    public Result assignManuscript() {
        try {
            List<ManuscriptSimpleDTO> list = manuService.getManuscriptsByStatus("AwaitingAssigning");
            return Result.success(list);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/chair/assign/reviewer")
    public Result assignReviewer() {
        try {
            List<UserSimpleDTO> list = userService.getUsersByRole("ROLE_REVIEWER");
            return Result.success(list);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @PostMapping("/chair/assign/confirm")
    public Result confirmAssignManuscript(@RequestParam Integer manuId, @RequestParam Integer userId) {
        try {
            manuService.confirmAssignManuscript(manuId, userId);
            return Result.success("稿件分配确认成功");
        } catch (Exception e) {
            return Result.error("确认分配失败：" + e.getMessage());
        }
    }


    @GetMapping("/review/get/{reviewerId}")
    public Result getManuscriptsForReview(@PathVariable Integer reviewerId) {
        try {
            List<ManuscriptSimpleDTO> list = manuService.getManuscriptsForReview(reviewerId);
            return Result.success(list);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/review/check/{reviewerId}")
    public Result getReviewedManuscripts(@PathVariable Integer reviewerId) {
        try {
            List<ManuscriptReviewDTO> list = manuService.getReviewedManuscripts(reviewerId);
            return Result.success(list);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/review/get-single/{manuId}")
    public Result getSingleReviewManu(@PathVariable Integer manuId) {
        try {
            ManuscriptReviewDTO dto = manuService.getSingleReviewManu(manuId);
            return Result.success(dto);
        } catch (Exception e) {
            return Result.error("获取稿件信息失败：" + e.getMessage());
        }
    }

    @GetMapping("/detail/get-single/{manuId}")
    public Result getSingleManuDetail(@PathVariable Integer manuId) {
        try {
            System.out.println("=== Getting manuscript detail for ID: " + manuId);
            ManuscriptReviewDTO dto = manuService.getSingleReviewManu(manuId);
            System.out.println("=== Successfully retrieved manuscript: " + dto.getTitle());
            return Result.success(dto);
        } catch (Exception e) {
            System.err.println("=== Error getting manuscript detail: " + e.getMessage());
            e.printStackTrace();
            return Result.error("获取稿件信息失败：" + e.getMessage());
        }
    }

    @PostMapping("/review/submit/{manuId}")
    public Result submitReview(
            @PathVariable Integer manuId,
            @RequestParam String result,
            @RequestParam(required = false) String grade
    ) {
        try {
            Manuscript manuscript = manuService.submitReview(manuId, result, grade);
            return Result.success("审稿提交成功", manuscript);
        } catch (Exception e) {
            return Result.error("审稿提交失败：" + e.getMessage());
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
                List<ManuscriptSimpleDTO> list = manuService.getManuscriptsByStatus(status);
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

