<script setup lang="ts">
import Header from '@/layouts/Header.vue'
import { computed, onMounted } from 'vue'
import { marked } from 'marked'
import { useApi } from '@/utils/useApi'

const { data, fetchData, loading, error } = useApi()

const page = computed(() => data.value?.data || data.value || null)

const htmlContent = computed(() => {
  return page.value?.content ? marked.parse(page.value.content) : ''
})

onMounted(() => {
  fetchData('/api/pages/about')
})
</script>

<template>
  <div class="page-wrapper">
    <Header />

    <main class="page">
      <h1>{{ page?.title || 'About' }}</h1>

      <p v-if="loading">Loading...</p>
      <p v-else-if="error">Failed to load</p>

      <div v-else v-html="htmlContent" class="markdown-body"></div>
    </main>
  </div>
</template>

<style scoped>
.page-wrapper {
  min-height: 100vh;
}

.page {
  padding: 80px 24px;
  max-width: 900px;
  margin: 0 auto;
}

.markdown-body {
  line-height: 1.8;
}
</style>