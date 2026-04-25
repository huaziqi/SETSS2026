<script setup lang="ts">
import PostCard from './PostCard.vue'

defineProps<{
  posts: any[]
}>()
</script>

<template>
  <section class="section">
    <div class="section-header">
      <h2>Pinned Posts</h2>
    </div>

    <div class="post-list">
      <PostCard
        v-for="post in posts"
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