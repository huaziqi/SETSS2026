<script setup lang="ts">
import { computed } from 'vue'

interface PostItem {
  postId: number
  status: string
  viewCount: number
  isPinned: boolean
}

const props = defineProps<{ posts: PostItem[] }>()

const postStats = computed(() => {
  const total = props.posts.length
  const pinned = props.posts.filter(p => p.isPinned).length
  const draft = props.posts.filter(p => p.status === 'DRAFT').length
  const totalViews = props.posts.reduce((sum, p) => sum + (p.viewCount || 0), 0)
  return { total, pinned, draft, totalViews }
})
</script>

<template>
  <section class="panel">
    <h2>Dashboard</h2>
    <div class="stats-grid">
      <div class="stat-card"><span>Total Posts</span><strong>{{ postStats.total }}</strong></div>
      <div class="stat-card"><span>Pinned Posts</span><strong>{{ postStats.pinned }}</strong></div>
      <div class="stat-card"><span>Draft Posts</span><strong>{{ postStats.draft }}</strong></div>
      <div class="stat-card"><span>Total Views</span><strong>{{ postStats.totalViews }}</strong></div>
    </div>
  </section>
</template>

<style scoped>
.panel { background: #fff; border: 1px solid #e5e5e5; padding: 24px; }
.panel h2 { margin: 0 0 14px; }
.stats-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 14px; }
.stat-card { background: #fafafa; border: 1px solid #ececec; border-radius: 8px; padding: 14px; display: flex; flex-direction: column; gap: 8px; }
.stat-card strong { font-size: 24px; }
</style>
