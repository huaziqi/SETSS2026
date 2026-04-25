<script setup lang="ts">
import PostCard from './PostCard.vue'

const pinnedPosts = [
  {
    postId: 1,
    title: 'SETSS2026 Registration Notice',
    content: 'Please read the registration instructions before submitting your information.',
    tag: 'Notice',
    publishTime: '2026-04-25',
    viewCount: 120,
    commentCount: 8,
    isPinned: true,
    username: 'Admin'
  },
  {
    postId: 2,
    title: 'Conference Schedule Discussion',
    content: 'This thread is for questions about the conference schedule and session arrangement.',
    tag: 'Schedule',
    publishTime: '2026-04-24',
    viewCount: 96,
    commentCount: 12,
    isPinned: true,
    username: 'Admin'
  }
]
</script>

<template>
  <section class="section">
    <div class="section-header">
      <h2>Pinned Posts</h2>
    </div>

    <div class="post-list">
      <PostCard
        v-for="post in pinnedPosts"
        :key="post.postId"
        :post="post"
      />
    </div>
  </section>
</template>

<style scoped>
.section {
  margin-bottom: 36px;
}

.section-header {
  margin-bottom: 14px;
}

.section-header h2 {
  margin: 0;
  font-size: 22px;
  color: #111;
}

.post-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
</style>