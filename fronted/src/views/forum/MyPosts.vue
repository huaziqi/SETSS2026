<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import Header from '@/layouts/Header.vue'
import { useApi } from '@/utils/useApi'

type PostItem = {
  postId: number
  title: string
  content: string
  tag?: string
  status: 'DRAFT' | 'PUBLISHED' | string
  publishTime: string
  updateTime?: string
  viewCount: number
  commentCount: number
}

const router = useRouter()
const { fetchData, putData, deleteData, validate, data, error, loading } = useApi()
const posts = ref<PostItem[]>([])

const sortedPosts = computed(() => {
  return [...posts.value].sort((a, b) => {
    return new Date(b.updateTime || b.publishTime).getTime() - new Date(a.updateTime || a.publishTime).getTime()
  })
})

const loadMyPosts = async () => {
  await fetchData('/api/posts/my')

  if (!error.value && Array.isArray(data.value)) {
    posts.value = data.value
  }
}

const goEdit = (postId: number) => {
  router.push(`/forum/write?postId=${postId}`)
}

const publishDraft = async (post: PostItem) => {
  await putData(`/api/posts/${post.postId}`, {
    status: 'PUBLISHED'
  })

  if (!error.value) {
    await loadMyPosts()
    alert('Draft published successfully.')
  }
}

const removePost = async (postId: number) => {
  const confirmed = window.confirm('Delete this post? This action cannot be undone.')
  if (!confirmed) return

  await deleteData(`/api/posts/${postId}`)

  if (!error.value) {
    posts.value = posts.value.filter(post => post.postId !== postId)
    alert('Post deleted successfully.')
  }
}

onMounted(async () => {
  const isValid = await validate()

  if (!isValid) {
    router.push('/login')
    return
  }

  await loadMyPosts()
})
</script>

<template>
  <div class="my-posts-page">
    <Header />

    <main class="my-posts-main">
      <div class="page-head">
        <h1>My Posts</h1>
        <button class="write-btn" @click="router.push('/forum/write')">Write New Post</button>
      </div>

      <p v-if="loading" class="hint">Loading your posts...</p>
      <p v-if="error" class="error">Failed to load posts. Please refresh and try again.</p>

      <div v-if="!loading && sortedPosts.length === 0" class="empty-card">
        <h2>No posts yet</h2>
        <p>Start by creating a draft or publishing your first post.</p>
      </div>

      <div class="post-list">
        <article v-for="post in sortedPosts" :key="post.postId" class="post-item">
          <header class="post-head">
            <h2>{{ post.title }}</h2>
            <span class="status" :class="post.status.toLowerCase()">{{ post.status }}</span>
          </header>

          <p class="content">{{ post.content }}</p>

          <div class="meta-row">
            <span>#{{ post.tag || 'General' }}</span>
            <span>Updated: {{ post.updateTime || post.publishTime }}</span>
            <span>{{ post.viewCount }} views</span>
            <span>{{ post.commentCount }} comments</span>
          </div>

          <div class="actions">
            <button class="btn" @click="goEdit(post.postId)">Edit</button>
            <button
              v-if="post.status === 'DRAFT'"
              class="btn solid"
              @click="publishDraft(post)"
            >
              Publish Draft
            </button>
            <button class="btn danger" @click="removePost(post.postId)">Delete</button>
          </div>
        </article>
      </div>
    </main>
  </div>
</template>

<style scoped>
.my-posts-page { min-height: 100vh; background: #f7f7f8; }
.my-posts-main { max-width: 980px; margin: 0 auto; padding: 30px 24px 60px; }
.page-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 18px; }
.page-head h1 { margin: 0; color: #111; }
.write-btn { border: 1px solid #111; background: #111; color: #fff; padding: 8px 14px; cursor: pointer; }
.hint { color: #666; }
.error { color: #c62828; }
.empty-card { border: 1px dashed #cfcfcf; background: #fff; padding: 26px; text-align: center; }
.post-list { display: flex; flex-direction: column; gap: 14px; }
.post-item { background: #fff; border: 1px solid #e5e5e5; padding: 18px; }
.post-head { display: flex; justify-content: space-between; align-items: center; gap: 12px; }
.post-head h2 { margin: 0; font-size: 20px; }
.status { padding: 4px 8px; font-size: 12px; border-radius: 999px; font-weight: 700; }
.status.published { background: #e7f6eb; color: #166534; }
.status.draft { background: #f3f4f6; color: #4b5563; }
.content { margin: 10px 0 14px; color: #4a4a4a; white-space: pre-wrap; }
.meta-row { display: flex; flex-wrap: wrap; gap: 14px; color: #777; font-size: 13px; }
.actions { margin-top: 14px; display: flex; gap: 10px; }
.btn { border: 1px solid #111; background: #fff; color: #111; padding: 7px 12px; cursor: pointer; }
.btn.solid { background: #111; color: #fff; }
.btn.danger { border-color: #c62828; color: #c62828; }
</style>
