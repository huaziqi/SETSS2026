<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'
import BaseButton from '@/components/BaseButton.vue'

defineProps<{
  tags: string[]
}>()

const router = useRouter()
const route = useRoute()

const goWrite = () => {
  router.push('/forum/write')
}

const goMyPosts = () => {
  router.push('/forum/my-posts')
}

const goTag = (tag: string) => {
  router.push({
    path: '/forum',
    query: { tag }
  })
}

const clearTag = () => {
  router.push('/forum')
}
</script>

<template>
  <aside class="forum-sidebar">
    <section class="side-card action-card">
      <div class="action-row">
        <BaseButton mode="dark" size="medium" @click="goWrite">
          Write
        </BaseButton>

        <BaseButton mode="dark" size="medium" @click="goMyPosts">
          My Posts
        </BaseButton>
      </div>
    </section>

    <section class="side-card">
      <div class="tag-head">
        <h3>Tags</h3>
        <button v-if="route.query.tag" class="clear-btn" @click="clearTag">
          Clear
        </button>
      </div>

      <div class="tag-list">
        <button
          v-for="tag in tags"
          :key="tag"
          class="tag-item"
          :class="{ active: route.query.tag === tag }"
          @click="goTag(tag)"
        >
          # {{ tag }}
        </button>
      </div>
    </section>
  </aside>
</template>

<style scoped>
.forum-sidebar {
  width: 280px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.side-card {
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 5px;
  padding: 20px;
}

.action-card {
  padding: 16px 20px;
}

.action-row {
  display: flex;
  gap: 10px;
}

.action-row :deep(button) {
  flex: 1;
}

.tag-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}

.tag-head h3 {
  margin: 0;
  font-size: 18px;
  color: #111;
}

.clear-btn {
  border: none;
  background: transparent;
  color: #666;
  cursor: pointer;
  font-size: 13px;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.tag-item {
  border: none;
  padding: 7px 12px;
  border-radius: 999px;
  background: #f1f1f1;
  color: #333;
  font-size: 13px;
  cursor: pointer;
}

.tag-item:hover,
.tag-item.active {
  background: #111;
  color: #fff;
}

@media (max-width: 900px) {
  .forum-sidebar {
    width: 100%;
  }
}
</style>