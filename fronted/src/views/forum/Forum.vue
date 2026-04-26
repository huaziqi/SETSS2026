<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import Header from '@/layouts/Header.vue'
import { useApi } from '@/utils/useApi'
import ForumSidebar from '@/layouts/forum/ForumSidebar.vue'
import PinnedPostSection from '@/layouts/forum/PinnedPostSection.vue'
import PostListSection from '@/layouts/forum/PostListSection.vue'

const route = useRoute()
const { fetchData, data, loading, error } = useApi()

const posts = computed(() => data.value || [])

const currentTag = computed(() => {
  return typeof route.query.tag === 'string' ? route.query.tag : ''
})

const filteredPosts = computed(() => {
  if (!currentTag.value) return posts.value

  return posts.value.filter((post: any) => {
    return post.tag === currentTag.value
  })
})

const pinnedPosts = computed(() => {
  return filteredPosts.value.filter((post: any) => post.isPinned)
})

const normalPosts = computed(() => {
  return filteredPosts.value.filter((post: any) => !post.isPinned)
})

const tags = computed(() => {
  const set = new Set<string>()

  posts.value.forEach((post: any) => {
    if (post.tag) set.add(post.tag)
  })

  return Array.from(set)
})

onMounted(() => {
  fetchData('/api/posts')
})
</script>

<template>
  <div class="forum-page">
    <Header />

    <main class="forum-main">


      <div v-if="loading" class="message">Loading posts...</div>
      <div v-else-if="error" class="message error">Failed to load posts.</div>

      <div v-else class="forum-layout">
        <div class="forum-content">
          <PinnedPostSection
            v-if="pinnedPosts.length"
            :posts="pinnedPosts"
          />

          <PostListSection
            :posts="normalPosts"
            :current-tag="currentTag"
          />
        </div>

        <ForumSidebar :tags="tags" />
      </div>
    </main>
  </div>
</template>

<style scoped>
.forum-page {
  min-height: 100vh;
  background: #f7f7f8;
}

.forum-main {
  max-width: 1180px;
  margin: 0 auto;
  padding: 32px 24px 60px;
}

.forum-hero {
  margin-bottom: 28px;
}

.forum-hero h1 {
  margin: 0;
  font-size: 34px;
  font-weight: 700;
  color: #111;
}

.forum-hero p {
  margin-top: 8px;
  color: #666;
  font-size: 15px;
}

.forum-layout {
  display: flex;
  align-items: flex-start;
  gap: 28px;
}

.forum-content {
  flex: 1;
  min-width: 0;
}

.message {
  padding: 24px;
  background: #fff;
  border: 1px solid #e5e5e5;
  color: #555;
}

.error {
  color: #c0392b;
}

@media (max-width: 900px) {
  .forum-layout {
    flex-direction: column;
  }
}
</style>