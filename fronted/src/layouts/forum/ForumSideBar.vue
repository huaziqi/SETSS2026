<script setup lang="ts">
import { useRouter } from 'vue-router'
import BaseButton from '@/components/BaseButton.vue'

const router = useRouter()

const tags = [
  'Notice',
  'Schedule',
  'Submission',
  'Keynote',
  'Travel',
  'Registration',
  'Course'
]

const goWrite = () => {
  router.push('/forum/write')
}

const goMyPosts = () => {
  router.push('/forum/my-posts')
}

const goTag = (tag: string) => {
  router.push({
    path: '/forum',
    query: {
      tag
    }
  })
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
      <h3>Tags</h3>

      <div class="tag-list">
        <button
          v-for="tag in tags"
          :key="tag"
          class="tag-item"
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
  padding: 16px 20px; /* 和下面 tag 区对齐 */
}

/* 横向布局 */
.action-row {
  display: flex;
  gap: 10px;
}

/* 让两个按钮等宽 */
.action-row :deep(button) {
  flex: 1;
}

.my-post-btn {
  width: 100%;
  height: 42px;
  border: 1px solid #111;
  border-radius: 5px;
  background: #fff;
  color: #111;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.my-post-btn:hover {
  background: #111;
  color: #fff;
}

.side-card h3 {
  margin: 0 0 14px;
  font-size: 18px;
  color: #111;
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
  transition: all 0.2s ease;
}

.tag-item:hover {
  background: #111;
  color: #fff;
}
</style>