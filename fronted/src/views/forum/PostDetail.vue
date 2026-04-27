<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Header from '@/layouts/Header.vue'
import { useApi } from '@/utils/useApi'
import CommentSection from '@/components/forum/CommentSection.vue'

const route = useRoute()
const router = useRouter()
const { fetchData, data, error, loading } = useApi()

const postId = computed(() => Number(route.params.postId))
const post = computed(() => data.value)

const formatTime = (time?: string) => {
  if (!time) return '-'
  return new Date(time).toLocaleString()
}
onMounted(async () => {
  if (Number.isNaN(postId.value)) {
    router.push('/forum')
    return
  }

  const anchorId = route.query.anchorId
  const blockStart = route.query.blockStart
  const blockEnd = route.query.blockEnd

  let url = `/api/posts/${postId.value}`

  const params = new URLSearchParams()

  if (typeof anchorId === 'string') {
    params.append('anchorId', anchorId)
  }

  if (typeof blockStart === 'string') {
    params.append('blockStart', blockStart)
  }

  if (typeof blockEnd === 'string') {
    params.append('blockEnd', blockEnd)
  }

  if (params.toString()) {
    url += `?${params.toString()}`
  }

  await fetchData(url)

})
</script>

<template>
  <div class="post-detail-page">
    <Header />

    <main class="post-detail-main">
      <button class="back-btn" @click="router.push('/forum')">
        Back to Forum
      </button>

      <div v-if="loading" class="message">Loading post...</div>
      <div v-else-if="error" class="message error">Failed to load post.</div>

      <article v-else-if="post" class="post-card">
        <h1>{{ post.title }}</h1>

        <div class="meta-row">
          <span>Author: {{ post.userName || 'Anonymous' }}</span>
          <span>Published: {{ formatTime(post.publishTime) }}</span>
          <span>Views: {{ post.viewCount }}</span>
          <span>Comments: {{ post.commentCount }}</span>
          <span v-if="post.tag">#{{ post.tag }}</span>
        </div>

        <div class="content html-content" v-html="post.htmlContent"></div>

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

.html-content {
  line-height: 1.8;
  color: #1f2937;
  border-top: 1px solid #e5e7eb;
  padding-top: 16px;
}

.html-content :deep(.md-block) {
  scroll-margin-top: 96px;
  padding: 2px 0;
}


.html-content :deep(h1),
.html-content :deep(h2),
.html-content :deep(h3) {
  margin-top: 1.2em;
  margin-bottom: 0.6em;
}

.html-content :deep(p) {
  margin: 0.6em 0;
}

.html-content :deep(pre) {
  background: #f6f6f6;
  padding: 12px;
  overflow-x: auto;
}

.html-content :deep(code) {
  background: #f3f4f6;
  padding: 2px 4px;
}

</style>