<script setup lang="ts">
import Header from '@/layouts/Header.vue'
import { computed, onMounted } from 'vue'
import { useApi } from '@/utils/useApi'

const { data, fetchData, loading, error } = useApi()

const page = computed(() => data.value?.data || data.value || null)

const rawHtmlContent = computed(() => {
  return page.value?.htmlContent || ''
})

const days = computed(() => {
  const matches = [...rawHtmlContent.value.matchAll(/<h2>(.*?)<\/h2>/g)]

  return matches.map((match, index) => {
    const title = match[1]
    return {
      id: `day-${index + 1}`,
      title
    }
  })
})

const htmlContent = computed(() => {
  let index = 0

  return rawHtmlContent.value.replace(/<h2>(.*?)<\/h2>/g, (_, title) => {
    index++
    return `<h2 id="day-${index}" class="schedule-day-title">${title}</h2>`
  })
})

const scrollToDay = (id: string) => {
  const el = document.getElementById(id)
  if (!el) return

  el.scrollIntoView({
    behavior: 'smooth',
    block: 'start'
  })
}

onMounted(() => {
  fetchData('/api/admin/pages/schedule')
})
</script>

<template>
  <div class="page-wrapper">
    <Header />

    <main class="schedule-page">
      <section class="schedule-main">
        <p v-if="loading" class="state-text">Loading...</p>
        <p v-else-if="error" class="state-text error">Failed to load</p>

        <div
          v-else
          class="markdown-body"
          v-html="htmlContent"
        ></div>
      </section>

      <aside v-if="days.length" class="day-nav">
        <div class="day-nav-inner">
          <div class="nav-title">Schedule</div>

          <button
            v-for="day in days"
            :key="day.id"
            class="nav-item"
            @click="scrollToDay(day.id)"
          >
            {{ day.title }}
          </button>
        </div>
      </aside>
    </main>
  </div>
</template>

<style scoped>
.page-wrapper {
  min-height: 100vh;
  background: #f7f7f8;
}

.schedule-page {
  max-width: 1180px;
  margin: 0 auto;
  padding: 76px 24px 64px;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 190px;
  gap: 28px;
  align-items: flex-start;
}

.schedule-main {
  min-width: 0;
}

.state-text {
  background: #fff;
  padding: 24px;
  color: #555;
}

.error {
  color: #a11313;
}

.markdown-body {
  line-height: 1.75;
  background: #fff;
  padding: 32px 36px;
  color: #222;
  font-size: 15px;
}

.markdown-body :deep(h1) {
  margin: 0 0 28px;
  font-size: 34px;
  font-weight: 500;
  letter-spacing: -0.03em;
  color: #111;
}

.markdown-body :deep(h2) {
  scroll-margin-top: 90px;
  margin: 38px 0 16px;
  padding-top: 4px;
  font-size: 22px;
  font-weight: 500;
  color: #111;
}

.markdown-body :deep(h2:first-of-type) {
  margin-top: 0;
}

.markdown-body :deep(table) {
  width: 100%;
  table-layout: fixed;
  border-collapse: separate;
  border-spacing: 0;
  margin: 16px 0 30px;
  font-size: 14px;
  background: #fff;
}

.markdown-body :deep(th),
.markdown-body :deep(td) {
  padding: 14px 16px;
  text-align: left;
  vertical-align: top;
  border-bottom: 1px solid #ececef;
}

.markdown-body :deep(th) {
  background: #f5f5f6;
  color: #333;
  font-weight: 600;
  font-size: 13px;
}

.markdown-body :deep(th:first-child),
.markdown-body :deep(td:first-child) {
  width: 132px;
  min-width: 132px;
  max-width: 132px;
  white-space: nowrap;
  color: #111;
  font-weight: 500;
  font-variant-numeric: tabular-nums;
}

.markdown-body :deep(th:first-child) {
  border-top-left-radius: 6px;
}

.markdown-body :deep(th:last-child) {
  border-top-right-radius: 6px;
}

.markdown-body :deep(tr:hover td) {
  background: #fafafa;
}

.markdown-body :deep(hr) {
  border: none;
  height: 1px;
  background: #eeeeef;
  margin: 34px 0;
}

.day-nav {
  position: sticky;
  top: 88px;
}

.day-nav-inner {
  background: #fff;
  padding: 14px;
}

.nav-title {
  font-size: 13px;
  color: #777;
  margin-bottom: 10px;
}

.nav-item {
  width: 100%;
  border: none;
  background: transparent;
  padding: 9px 10px;
  text-align: left;
  color: #333;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.nav-item:hover {
  background: #f2f2f3;
  color: #000;
}

@media (max-width: 900px) {
  .schedule-page {
    display: block;
    padding: 70px 16px 48px;
  }

  .day-nav {
    display: none;
  }

  .markdown-body {
    padding: 24px 18px;
  }

  .markdown-body :deep(table) {
    display: block;
    overflow-x: auto;
  }

  .markdown-body :deep(th:first-child),
  .markdown-body :deep(td:first-child) {
    width: 120px;
    min-width: 120px;
    max-width: 120px;
  }
}
</style>