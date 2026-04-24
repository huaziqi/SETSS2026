package com.LHZ.SETSS2026.service;

import com.LHZ.SETSS2026.entity.Manuscript;
import com.LHZ.SETSS2026.repository.ManuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ManuService {
    private final ManuRepository manuRepository;

    //提交新稿件
    public Manuscript submitManuscript(Manuscript manuscript){
        manuscript.setStatus("待审核");
        manuscript.setPublishTime(LocalDateTime.now());
        manuscript.setUpdateTime(LocalDateTime.now());
        return manuRepository.save(manuscript);
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
        existingManuscript.setContent(manuscript.getContent());
        existingManuscript.setAuthor(manuscript.getAuthor());
        existingManuscript.setStatus(manuscript.getStatus());
        existingManuscript.setReviewer(manuscript.getReviewer());
        existingManuscript.setReviewResult(manuscript.getReviewResult());
        existingManuscript.setUpdateTime(LocalDateTime.now());

        return manuRepository.save(existingManuscript);
    }

    //删除稿件
    public void deleteManuscript(Integer manuscriptId){
        Manuscript manuscript = manuRepository.findById(manuscriptId).orElseThrow(() -> new RuntimeException("稿件不存在"));
        manuRepository.delete(manuscript);
    }

}
