<script setup lang="ts">
defineProps<{
  post: {
    postId: number
    title: string
    content: string
    tag?: string
    publishTime: string
    viewCount: number
    commentCount: number
    isPinned?: boolean
    username?: string
  }
}>()
</script>

<template>
  <article class="post-card">
    <div class="post-main">
      <div class="post-title-row">
        <span v-if="post.isPinned" class="pinned">Top</span>
        <h3>{{ post.title }}</h3>
      </div>

      <p class="post-content">
        {{ post.content }}
      </p>

      <div class="post-meta">
        <span>{{ post.username || 'Anonymous' }}</span>
        <span>{{ post.publishTime }}</span>
        <span v-if="post.tag">#{{ post.tag }}</span>
      </div>
    </div>

    <div class="post-stats">
      <span>{{ post.viewCount }} views</span>
      <span>{{ post.commentCount }} comments</span>
    </div>
  </article>
</template>

<style scoped>
.post-card {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  padding: 20px 22px;
  background: #fff;
  /* border-radius: 14px; */
  border: 1px solid #e8e8e8;
  transition: all 0.2s ease;
}

.post-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.06);
}

.post-main {
  min-width: 0;
}

.post-title-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.post-title-row h3 {
  margin: 0;
  font-size: 18px;
  color: #111;
}

.pinned {
  padding: 3px 8px;
  border-radius: 999px;
  font-size: 12px;
  color: #fff;
  background: #111;
}

.post-content {
  margin: 10px 0;
  color: #555;
  font-size: 14px;
  line-height: 1.6;

  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  font-size: 13px;
  color: #888;
}

.post-stats {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
  color: #666;
  font-size: 13px;
  text-align: right;
}
</style>