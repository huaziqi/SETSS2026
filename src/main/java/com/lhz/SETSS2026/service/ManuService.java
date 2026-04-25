package com.LHZ.SETSS2026.service;

import com.LHZ.SETSS2026.entity.Manuscript;
import com.LHZ.SETSS2026.repository.ManuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ManuService {
    private final ManuRepository manuRepository;

    @Value("${file.upload-dir:uploads/manuscripts}")
    private String uploadDir;

    //提交新稿件
    //提交新稿件（带文件）
    @Transactional
    public Manuscript submitManuscript(Manuscript manuscript, MultipartFile file) throws IOException {
        // 验证文件
        validateFile(file);

        // 保存文件到本地
        String savedFilePath = saveFile(file);

        // 设置稿件信息
        manuscript.setStatus("待审核");
        manuscript.setPublishTime(LocalDateTime.now());
        manuscript.setUpdateTime(LocalDateTime.now());
        manuscript.setOriginalFileName(file.getOriginalFilename());
        manuscript.setFilePath(savedFilePath);
        manuscript.setFileType(getFileExtension(file.getOriginalFilename()));
        manuscript.setFileSize(file.getSize());

        return manuRepository.save(manuscript);
    }

    // 验证文件类型和大小
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


    // 保存文件并返回相对路径
    private String saveFile(MultipartFile file) throws IOException {
        // 创建上传目录
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 生成唯一文件名避免冲突
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String uniqueFilename = UUID.randomUUID().toString() + "." + extension;

        // 保存文件
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath);

        // 返回相对路径（便于访问）
        return uploadDir + "/" + uniqueFilename;
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

    //根据状态查询稿件
    public List<Manuscript> getManuscriptsByStatus(String status){
        return manuRepository.findByStatus(status);
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
        existingManuscript.setReviewer(manuscript.getReviewer());
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

}
