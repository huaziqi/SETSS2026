<script setup lang="ts">
import { computed, ref } from 'vue'
import PostCard from './PostCard.vue'

const sortType = ref<'time' | 'views'>('time')

const posts = ref([
  {
    postId: 3,
    title: 'How to submit a paper?',
    content: 'I want to know where I can submit my paper and whether there is a template.',
    tag: 'Submission',
    publishTime: '2026-04-23',
    viewCount: 45,
    commentCount: 3,
    username: 'Alice'
  },
  {
    postId: 4,
    title: 'Question about keynote speeches',
    content: 'Will keynote speeches be available online after the conference?',
    tag: 'Keynote',
    publishTime: '2026-04-22',
    viewCount: 76,
    commentCount: 6,
    username: 'Bob'
  },
  {
    postId: 5,
    title: 'Hotel recommendation near the venue',
    content: 'Are there any recommended hotels near the conference venue?',
    tag: 'Travel',
    publishTime: '2026-04-21',
    viewCount: 101,
    commentCount: 9,
    username: 'Cindy'
  }
])

const sortedPosts = computed(() => {
  const list = [...posts.value]

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
      <h2>All Posts</h2>

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

    <div class="post-list">
      <PostCard
        v-for="post in sortedPosts"
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
</style>