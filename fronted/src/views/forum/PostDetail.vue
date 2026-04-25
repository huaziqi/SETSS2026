<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Header from '@/layouts/Header.vue'
import { useApi } from '@/utils/useApi'
import CommentSection from '@/components/forum/CommentSection.vue'

const route = useRoute()
const router = useRouter()
const { fetchData, data, error, loading, validate } = useApi()

const postId = computed(() => Number(route.params.postId))
const post = computed(() => data.value)

const formatTime = (time?: string) => {
  if (!time) return '-'
  return new Date(time).toLocaleString()
}

onMounted(async () => {
  const isValid = await validate()
  if (!isValid) {
    router.push('/login')
    return
  }

  if (Number.isNaN(postId.value)) {
    router.push('/forum')
    return
  }

  await fetchData(`/api/posts/${postId.value}`)
})
</script>

<template>
  <div class="post-detail-page">
    <Header />

    <main class="post-detail-main">
      <button class="back-btn" @click="router.push('/forum')">返回论坛</button>

      <div v-if="loading" class="message">文章加载中...</div>
      <div v-else-if="error" class="message error">文章加载失败。</div>

      <article v-else-if="post" class="post-card">
        <h1>{{ post.title }}</h1>

        <div class="meta-row">
          <span>作者：{{ post.userName || '匿名用户' }}</span>
          <span>发布时间：{{ formatTime(post.publishTime) }}</span>
          <span>浏览：{{ post.viewCount }}</span>
          <span>评论：{{ post.commentCount }}</span>
          <span v-if="post.tag">#{{ post.tag }}</span>
        </div>

        <div class="content">{{ post.content }}</div>

        <CommentSection :post-id="postId" />
      </article>
    </main>
  </div>
</template>

<style scoped>
.post-detail-page {
  min-height: 100vh;
  background: #f7f7f8;
}
.post-detail-main {
  max-width: 980px;
  margin: 0 auto;
  padding: 32px 24px 60px;
}
.back-btn {
  border: 1px solid #111;
  background: #fff;
  padding: 8px 14px;
  cursor: pointer;
  margin-bottom: 16px;
}
.post-card {
  border: 1px solid #e5e7eb;
  background: #fff;
  padding: 22px;
}
.post-card h1 {
  margin: 0 0 12px;
}
.meta-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 16px;
}
.content {
  white-space: pre-wrap;
  line-height: 1.8;
  color: #1f2937;
  border-top: 1px solid #e5e7eb;
  padding-top: 16px;
}
.message {
  padding: 16px;
  background: #fff;
  border: 1px solid #e5e7eb;
}
.error {
  color: #dc2626;
}
</style>
