<script setup lang="ts">
import Header from '@/layouts/Header.vue'
import { computed, onMounted } from 'vue'
import { marked } from 'marked'
import { useRouter } from 'vue-router'
import { useApi } from '@/utils/useApi'

const router = useRouter()
const { data, fetchData, loading, error } = useApi()

const page = computed(() => data.value?.data || data.value || null)

const htmlContent = computed(() => {
  return page.value?.content ? marked.parse(page.value.content) : ''
})

const goPage = (path: string) => {
  router.push(path)
}

onMounted(() => {
  fetchData('/api/admin/pages/home')
})
</script>

<template>
  <div class="page-wrapper">
    <Header />

    <main class="home">
      <!-- 标题 -->
      <section class="hero">
        <h1>{{ page?.title || 'SETSS 2026' }}</h1>
      </section>

      <!-- Markdown 内容 -->
      <section class="content">
        <p v-if="loading">Loading...</p>
        <p v-else-if="error" class="error">Failed to load</p>

        <div
          v-else
          class="markdown-body"
          v-html="htmlContent"
        ></div>
      </section>

      <!-- 快捷入口 -->
      <section class="quick-links">
        <button @click="goPage('/schedule')">Schedule</button>
        <button class="outline" @click="goPage('/courses')">Courses</button>
        <button class="outline" @click="goPage('/about')">About</button>
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

.hero {
  text-align: center;
  margin-bottom: 30px;
}

.hero h1 {
  font-size: 48px;
  margin: 0;
}

.content {
  border: 1px solid #e5e5e5;
  padding: 24px;
  background: #fff;
}

.markdown-body {
  line-height: 1.8;
  font-size: 16px;
}

.quick-links {
  margin-top: 30px;
  display: flex;
  justify-content: center;
  gap: 12px;
}

button {
  border: 1px solid #111;
  background: #111;
  color: #fff;
  padding: 10px 18px;
  cursor: pointer;
}

button.outline {
  background: #fff;
  color: #111;
}

.error {
  color: #a11313;
}
</style>