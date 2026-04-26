<script setup lang="ts">
import { computed, ref } from 'vue'
import PostCard from './PostCard.vue'

const props = defineProps<{
  posts: any[]
  currentTag?: string
}>()

const sortType = ref<'time' | 'views'>('time')

const sortedPosts = computed(() => {
  const list = [...props.posts]

  if (sortType.value === 'views') {
    return list.sort((a, b) => b.viewCount - a.viewCount)
  }

  return list.sort(
    (a, b) =>
      new Date(b.publishTime).getTime() - new Date(a.publishTime).getTime()
  )
})
</script>

<template>
  <section class="section">
    <div class="section-header">
      <div>
        <h2>
          {{ currentTag ? `# ${currentTag}` : 'All Posts' }}
        </h2>
        <p v-if="currentTag" class="filter-tip">
          Showing posts tagged with {{ currentTag }}
        </p>
      </div>

      <div class="sort-group">
        <button
          :class="{ active: sortType === 'time' }"
          @click="sortType = 'time'"
        >
          Latest
        </button>

        <button
          :class="{ active: sortType === 'views' }"
          @click="sortType = 'views'"
        >
          Most Viewed
        </button>
      </div>
    </div>

    <div v-if="sortedPosts.length" class="post-list">
      <PostCard
        v-for="post in sortedPosts"
        :key="post.postId"
        :post="post"
      />
    </div>

    <div v-else class="empty">
      No posts found.
    </div>
  </section>
</template>

<style scoped>
.section {
  margin-bottom: 36px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-header h2 {
  margin: 0;
  font-size: 22px;
  color: #111;
}

.filter-tip {
  margin: 6px 0 0;
  font-size: 13px;
  color: #777;
}

.sort-group {
  display: flex;
  gap: 8px;
  padding: 4px;
  border-radius: 999px;
  background: #eee;
}

.sort-group button {
  border: none;
  padding: 7px 14px;
  border-radius: 999px;
  background: transparent;
  cursor: pointer;
  color: #555;
  font-size: 13px;
}

.sort-group button.active {
  background: #111;
  color: #fff;
}

.post-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.empty {
  padding: 24px;
  background: #fff;
  border: 1px solid #e8e8e8;
  color: #666;
}
</style>