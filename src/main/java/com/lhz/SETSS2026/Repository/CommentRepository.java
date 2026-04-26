package com.LHZ.SETSS2026.repository;

import com.LHZ.SETSS2026.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPost_PostIdOrderByPublishTimeAsc(Long postId);

    List<Comment> findByPost_PostIdOrderByPublishTimeDesc(Long postId);

    List<Comment> findAllByOrderByPublishTimeDesc();

    List<Comment> findByParentComment_CommentId(Long parentCommentId);

    long countByPost_PostId(Long postId);
}
