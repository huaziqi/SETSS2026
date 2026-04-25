package com.LHZ.SETSS2026.repository;

import com.LHZ.SETSS2026.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findByTitle(String title);

    // 根据用户ID查
    List<Post> findByUser_Id(Integer userId);
}