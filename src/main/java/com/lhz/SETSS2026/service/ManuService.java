package com.LHZ.SETSS2026.service;

import com.LHZ.SETSS2026.dto.Manuscripts.ManuscriptSimpleDTO;
import com.LHZ.SETSS2026.entity.Manuscript;
import com.LHZ.SETSS2026.enums.ManuscriptGrade;
import com.LHZ.SETSS2026.enums.ManuscriptStatus;
import com.LHZ.SETSS2026.repository.ManuRepository;
import com.LHZ.SETSS2026.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ManuService {
    private final ManuRepository manuRepository;
    private final UserRepository userRepository;

    @Value("${file.upload-dir:uploads/manuscripts}")
    private String uploadDir;

    //提交新稿件
    @Transactional
    public Manuscript submitManuscript(Manuscript manuscript, MultipartFile file) throws IOException {
        Integer userId = manuscript.getUserId();

        if (userId == null) {
            throw new RuntimeException("用户ID不能为空");
        }

        List<Manuscript> existingManuscripts = manuRepository.findByUserId(userId);
        if (existingManuscripts.size() >= 5) {
            throw new RuntimeException("每个用户最多只能上传5个稿件，当前已有" + existingManuscripts.size() + "个");
        }

        validateFile(file);

        String savedFilePath = saveFile(file, userId);

        manuscript.setStatus(ManuscriptStatus.AwaitingChecking);
        manuscript.setPublishTime(LocalDateTime.now());
        manuscript.setUpdateTime(LocalDateTime.now());
        manuscript.setOriginalFileName(file.getOriginalFilename());
        manuscript.setFilePath(savedFilePath);
        manuscript.setFileType(getFileExtension(file.getOriginalFilename()));
        manuscript.setFileSize(file.getSize());

        return manuRepository.save(manuscript);
    }

    // 保存文件并返回相对路径
    private String saveFile(MultipartFile file, Integer userId) throws IOException {
        Path uploadPath = Paths.get(uploadDir, String.valueOf(userId));
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String uniqueFilename = UUID.randomUUID().toString() + "." + extension;

        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath);

        return uploadDir + "/" + userId + "/" + uniqueFilename;
    }

    // 验证文件类型和大小
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new RuntimeException("文件格式不正确");
        }

        String extension = getFileExtension(originalFilename).toLowerCase();

        // 允许的文件扩展名
        List<String> allowedExtensions = List.of(
                "jpg", "jpeg", "png", "gif", "bmp", "webp",  // 图片格式
                "docx",                                        // Word文档
                "pdf",                                         // PDF文档
                "md", "markdown",                              // Markdown文档
                "zip"                                          // 压缩文件
        );

        if (!allowedExtensions.contains(extension)) {
            throw new RuntimeException("只支持图片(jpg/png/gif/bmp/webp)、DOCX、PDF、Markdown、ZIP格式的文件");
        }

        // 限制文件大小（例如 50MB）
        if (file.getSize() > 50 * 1024 * 1024) {
            throw new RuntimeException("文件大小不能超过 50MB");
        }
    }


    // 获取文件扩展名
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    //查询所有稿件
    public List<Manuscript> getAllManuscripts(){
        return manuRepository.findAll();
    }

    //根据ID查询稿件
    public Optional<Manuscript> getManuscriptById(Integer manuscriptId){
        return manuRepository.findById(manuscriptId);
    }


    //根据审稿人查询稿件
    public List<Manuscript> getManuscriptsByReviewer(String reviewer){
        return manuRepository.findByReviewer(reviewer);
    }

    //根据作者查询稿件
    public List<Manuscript> getManuscriptsByAuthor(String author){
        return manuRepository.findByAuthor(author);
    }

    //更新稿件
    @Transactional
    public Manuscript updateManuscript(Integer manuscriptId, Manuscript manuscript){
        Manuscript existingManuscript = manuRepository.findById(manuscriptId).orElseThrow(() -> new RuntimeException("稿件不存在"));

        existingManuscript.setTitle(manuscript.getTitle());
        existingManuscript.setIntroduction(manuscript.getIntroduction());
        existingManuscript.setAuthor(manuscript.getAuthor());
        existingManuscript.setStatus(manuscript.getStatus());
        existingManuscript.setReviewerId(manuscript.getReviewerId());
        existingManuscript.setReviewResult(manuscript.getReviewResult());
        existingManuscript.setUpdateTime(LocalDateTime.now());

        return manuRepository.save(existingManuscript);
    }

    //删除稿件（同时删除文件）
    @Transactional
    public void deleteManuscript(Integer manuscriptId){
        Manuscript manuscript = manuRepository.findById(manuscriptId)
                .orElseThrow(() -> new RuntimeException("稿件不存在"));

        // 删除物理文件
        if (manuscript.getFilePath() != null) {
            try {
                Path filePath = Paths.get(manuscript.getFilePath());
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                // 记录日志但不阻止删除操作
                System.err.println("删除文件失败：" + e.getMessage());
            }
        }

        manuRepository.delete(manuscript);
    }

    //根据用户ID查询稿件（精简版）
    public List<ManuscriptSimpleDTO> getManuscriptsByUserId(Integer userId){
        return manuRepository.findByUserId(userId).stream()
                .map(this::convertToSimpleDTO)
                .collect(Collectors.toList());
    }

    private ManuscriptSimpleDTO convertToSimpleDTO(Manuscript manuscript) {
        ManuscriptSimpleDTO dto = new ManuscriptSimpleDTO();
        dto.setManuscriptId(manuscript.getManuscriptId());
        dto.setTitle(manuscript.getTitle());
        dto.setAuthor(manuscript.getAuthor());
        dto.setIntroduction(manuscript.getIntroduction());
        dto.setOriginalFileName(manuscript.getOriginalFileName());
        dto.setPublishTime(manuscript.getPublishTime());
        dto.setUpdateTime(manuscript.getUpdateTime());
        dto.setStatus(manuscript.getStatus() != null ? manuscript.getStatus().getDescription() : null);
        return dto;
    }

    public ManuscriptSimpleDTO getManuscriptForUpdate(Integer manuscriptId) {
        Manuscript manuscript = manuRepository.findById(manuscriptId)
                .orElseThrow(() -> new RuntimeException("稿件不存在"));

        return convertToSimpleDTO(manuscript);
    }

    public void downloadManuscriptFile(Integer manuscriptId, HttpServletResponse response) throws IOException {
        Manuscript manuscript = manuRepository.findById(manuscriptId)
                .orElseThrow(() -> new RuntimeException("稿件不存在"));

        if (manuscript.getFilePath() == null || manuscript.getFilePath().isEmpty()) {
            throw new RuntimeException("文件路径不存在");
        }

        Path filePath = Paths.get(manuscript.getFilePath());
        if (!Files.exists(filePath)) {
            throw new RuntimeException("文件不存在");
        }

        String fileName = manuscript.getOriginalFileName();
        String contentType = Files.probeContentType(filePath);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        response.setContentType(contentType);
        response.setHeader("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode(fileName, "UTF-8") + "\"");
        response.setContentLength((int) Files.size(filePath));

        Files.copy(filePath, response.getOutputStream());
        response.getOutputStream().flush();
    }

    @Transactional
    public Manuscript updateManuscriptSubmit(Integer manuscriptId, Manuscript manuscript, MultipartFile file) throws IOException {
        Manuscript existingManuscript = manuRepository.findById(manuscriptId)
                .orElseThrow(() -> new RuntimeException("稿件不存在"));

        existingManuscript.setTitle(manuscript.getTitle());
        existingManuscript.setIntroduction(manuscript.getIntroduction());
        existingManuscript.setAuthor(manuscript.getAuthor());
        existingManuscript.setUserId(manuscript.getUserId());
        existingManuscript.setUpdateTime(LocalDateTime.now());

        if (file != null && !file.isEmpty()) {
            validateFile(file);

            if (existingManuscript.getFilePath() != null) {
                try {
                    Path oldFilePath = Paths.get(existingManuscript.getFilePath());
                    Files.deleteIfExists(oldFilePath);
                } catch (IOException e) {
                    System.err.println("删除旧文件失败：" + e.getMessage());
                }
            }

            String savedFilePath = saveFile(file, manuscript.getUserId());
            existingManuscript.setOriginalFileName(file.getOriginalFilename());
            existingManuscript.setFilePath(savedFilePath);
            existingManuscript.setFileType(getFileExtension(file.getOriginalFilename()));
            existingManuscript.setFileSize(file.getSize());
        }

        return manuRepository.save(existingManuscript);
    }


    //根据状态查询稿件
    public List<ManuscriptSimpleDTO> getManuscriptsByStatus(String status){
        ManuscriptStatus manuscriptStatus = ManuscriptStatus.valueOf(status);
        return manuRepository.findByStatus(manuscriptStatus).stream()
                .map(this::convertToSimpleDTO)
                .collect(Collectors.toList());
    }


    @Transactional
    public Manuscript updateManuscriptStatus(Integer manuscriptId, ManuscriptStatus newStatus) {
        Manuscript manuscript = manuRepository.findById(manuscriptId)
                .orElseThrow(() -> new RuntimeException("稿件不存在"));

        manuscript.setStatus(newStatus);
        manuscript.setUpdateTime(LocalDateTime.now());

        return manuRepository.save(manuscript);
    }

    @Transactional
    public Manuscript rejectManuscriptWithReason(Integer manuscriptId, String rejectReason) {
        Manuscript manuscript = manuRepository.findById(manuscriptId)
                .orElseThrow(() -> new RuntimeException("稿件不存在"));

        manuscript.setStatus(ManuscriptStatus.NonCompliant);
        manuscript.setReviewResult(rejectReason);
        manuscript.setUpdateTime(LocalDateTime.now());

        return manuRepository.save(manuscript);
    }


    public ManuscriptSimpleDTO getManuscriptForChecking(Integer manuId) {
        Manuscript manuscript = manuRepository.findById(manuId)
                .orElseThrow(() -> new RuntimeException("稿件不存在"));

        return convertToSimpleDTO(manuscript);

    }


    //分配稿件
    @Transactional
    public Manuscript assignManuscript(Integer manuscriptId, Integer reviewerId) {
        Manuscript manuscript = manuRepository.findById(manuscriptId)
                .orElseThrow(() -> new RuntimeException("稿件不存在"));

        if (manuscript.getStatus() != ManuscriptStatus.AwaitingAssigning) {
            throw new RuntimeException("只能分配状态为'待分配'的稿件");
        }

        manuscript.setStatus(ManuscriptStatus.UnderAssigning);
        manuscript.setReviewerId(reviewerId);
        manuscript.setUpdateTime(LocalDateTime.now());

        return manuRepository.save(manuscript);
    }



    @Transactional
    public Manuscript setManuscriptGrade(Integer manuscriptId, ManuscriptGrade grade) {
        Manuscript manuscript = manuRepository.findById(manuscriptId)
                .orElseThrow(() -> new RuntimeException("稿件不存在"));

        if (manuscript.getStatus() != ManuscriptStatus.Reviewed) {
            throw new RuntimeException("只能对已评审的稿件设置评级");
        }

        manuscript.setGrade(grade);
        manuscript.setUpdateTime(LocalDateTime.now());

        return manuRepository.save(manuscript);
    }


    //获取待审核的稿件列表
    public List<Manuscript> getAwaitingCheckingManuscripts(){
        return manuRepository.findByStatus(ManuscriptStatus.AwaitingChecking);
    }

    //获取审核中的稿件列表
    public List<Manuscript> getUnderCheckingManuscripts(){
        return manuRepository.findByStatus(ManuscriptStatus.UnderChecking);
    }

    //获取待评审的稿件列表
    public List<Manuscript> getAwaitingReviewingManuscripts(){
        return manuRepository.findByStatus(ManuscriptStatus.AwaitingReviewing);
    }


}
