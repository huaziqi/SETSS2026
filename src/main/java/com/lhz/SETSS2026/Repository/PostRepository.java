package com.LHZ.SETSS2026.repository;

import com.LHZ.SETSS2026.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // 查询某个用户的所有帖子，按发布时间倒序
    List<Post> findByUser_IdOrderByPublishTimeDesc(Integer userId);

    // 获取帖子列表：置顶优先，然后按发布时间倒序
    List<Post> findAllByOrderByIsPinnedDescPublishTimeDesc();

    // 根据标题模糊查询，按发布时间倒序
    List<Post> findByTitleContainingOrderByPublishTimeDesc(String title);
}