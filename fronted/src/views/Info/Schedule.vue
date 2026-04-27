<script setup lang="ts">
import Header from '@/layouts/Header.vue'
import { computed, onMounted } from 'vue'
import { useApi } from '@/utils/useApi'

const { data, fetchData, loading, error } = useApi()

const page = computed(() => data.value?.data || data.value || null)

const htmlContent = computed(() => {
  return page.value?.htmlContent || ''
})

onMounted(() => {
  fetchData('/api/admin/pages/schedule')
})
</script>

<template>
  <div class="page-wrapper">
    <Header />

    <main class="page">
      <p v-if="loading">Loading...</p>
      <p v-else-if="error">Failed to load</p>

      <div v-else class="markdown-body" v-html="htmlContent"></div>
    </main>
  </div>
</template>

<style scoped>
.page-wrapper {
  min-height: 100vh;
  background: #f7f7f8;
}

.page {
  padding: 80px 24px;
  max-width: 980px;
  margin: 0 auto;
}

.markdown-body {
  line-height: 1.8;
  background: #fff;
  border: 1px solid #e5e7eb;
  padding: 24px;
}

.markdown-body :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 20px 0;
  font-size: 14px;
}

.markdown-body :deep(th),
.markdown-body :deep(td) {
  border: 1px solid #e5e7eb;
  padding: 10px 12px;
  text-align: left;
  vertical-align: top;
}

.markdown-body :deep(th) {
  background: #f3f4f6;
  font-weight: 600;
}

.markdown-body :deep(tr:nth-child(even)) {
  background: #fafafa;
}

.markdown-body :deep(h1) {
  margin-top: 0;
}

.markdown-body :deep(h2) {
  margin-top: 32px;
  padding-bottom: 6px;
  border-bottom: 1px solid #e5e7eb;
}
</style>