<script setup lang="ts">
import Header from '@/layouts/Header.vue'
import { computed, onMounted } from 'vue'
import { useApi } from '@/utils/useApi'

const { data, fetchData, loading, error } = useApi()

const page = computed(() => data.value?.data || data.value || null)

// ✅ 使用后端已经渲染好的 HTML
const htmlContent = computed(() => {
  return page.value?.htmlContent || ''
})

onMounted(() => {
  fetchData('/api/admin/pages/home')
})
</script>

<template>
  <div class="page-wrapper">
    <Header />

    <main class="home">
      <section class="content">
        <p v-if="loading">Loading...</p>
        <p v-else-if="error" class="error">Failed to load</p>

        <div
          v-else
          class="markdown-body"
          v-html="htmlContent"
        ></div>
      </section>
    </main>
  </div>
</template>

<style scoped>
.page-wrapper {
  min-height: 100vh;
}

.home {
  padding-top: 80px;
  max-width: 980px;
  margin: 0 auto;
}

.content {
  border: 1px solid #e5e5e5;
  padding: 24px;
  background: #fff;
}

/* Markdown 渲染样式 */
.markdown-body {
  line-height: 1.8;
  font-size: 16px;
}

/* 保留结构块样式（如果你后端有 md-block） */
.markdown-body :deep(.md-block) {
  margin-bottom: 8px;
}

.markdown-body :deep(h1),
.markdown-body :deep(h2),
.markdown-body :deep(h3) {
  margin-top: 1.2em;
  margin-bottom: 0.6em;
}

.markdown-body :deep(p) {
  margin: 0.6em 0;
}

.markdown-body :deep(pre) {
  background: #f6f6f6;
  padding: 12px;
  overflow-x: auto;
}

.markdown-body :deep(code) {
  background: #f3f4f6;
  padding: 2px 4px;
}

.error {
  color: #a11313;
}
</style>