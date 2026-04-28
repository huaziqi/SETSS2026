package com.LHZ.SETSS2026.repository;

import com.LHZ.SETSS2026.entity.Manuscript;
import com.LHZ.SETSS2026.enums.ManuscriptStatus;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ManuRepository extends JpaRepository<Manuscript, Integer> {
    @NotNull Optional<Manuscript> findById(@NotNull Integer manuscriptId);

    List<Manuscript> findByAuthor(String author);

    List<Manuscript> findByStatus(ManuscriptStatus status);

    List<Manuscript> findByReviewer(String reviewer);

    List<Manuscript> findByReviewerId(Integer reviewerId);

    List<Manuscript> findByReviewerIdAndStatus(Integer reviewerId, ManuscriptStatus status);

    List<Manuscript> findByReviewerIdAndStatusIn(Integer reviewerId, java.util.List<ManuscriptStatus> statuses);

    List<Manuscript> findByUserId(Integer userId);


    Optional<Manuscript> findByTitle(String title);
}
