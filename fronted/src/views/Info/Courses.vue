<script setup lang="ts">
import Header from '@/layouts/Header.vue'
import { computed, onMounted, ref, watch } from 'vue'
import { marked } from 'marked'
import { useApi } from '@/utils/useApi'

const { data, fetchData, loading, error } = useApi()

const activeIndex = ref(0)

const page = computed(() => data.value?.data || data.value || null)

const rawHtml = computed(() => {
  if (page.value?.htmlContent) {
    return page.value.htmlContent
  }

  if (page.value?.content) {
    return marked.parse(page.value.content) as string
  }

  return ''
})

interface CourseItem {
  title: string
  html: string
}

const courses = computed<CourseItem[]>(() => {
  const html = rawHtml.value
  if (!html) return []

  const withoutMainTitle = html
    .replace(/<h1>Courses<\/h1>/i, '')
    .trim()

  const parts = withoutMainTitle
    .split(/<hr\s*\/?>/i)
    .map(item => item.trim())
    .filter(Boolean)

  return parts.map(part => {
    const titleMatch = part.match(/<h2[^>]*>(.*?)<\/h2>/i)
    const title = titleMatch ? titleMatch[1] : 'Untitled Course'

    return {
      title,
      html: part
    }
  })
})

const currentCourse = computed(() => {
  return courses.value[activeIndex.value] || null
})

const selectCourse = (index: number) => {
  activeIndex.value = index
  window.scrollTo({
    top: 0,
    behavior: 'smooth'
  })
}

const prevCourse = () => {
  if (activeIndex.value > 0) {
    selectCourse(activeIndex.value - 1)
  }
}

const nextCourse = () => {
  if (activeIndex.value < courses.value.length - 1) {
    selectCourse(activeIndex.value + 1)
  }
}

watch(courses, () => {
  activeIndex.value = 0
})

onMounted(() => {
  fetchData('/api/admin/pages/courses')
})
</script>

<template>
  <div class="page-wrapper">
    <Header />

    <main class="course-page">
      <aside v-if="courses.length" class="course-sidebar">
        <div class="sidebar-inner">
          <div class="sidebar-title">Courses</div>

          <button
            v-for="(course, index) in courses"
            :key="course.title"
            class="course-tab"
            :class="{ active: activeIndex === index }"
            @click="selectCourse(index)"
          >
            <span class="course-number">
              {{ String(index + 1).padStart(2, '0') }}
            </span>
            <span class="course-title">
              {{ course.title }}
            </span>
          </button>
        </div>
      </aside>

      <section class="course-content">
        <p v-if="loading" class="state-text">Loading...</p>
        <p v-else-if="error" class="state-text error">Failed to load</p>

        <template v-else-if="currentCourse">
          <div
            class="markdown-body"
            v-html="currentCourse.html"
          ></div>

          <div class="course-actions">
            <button
              class="switch-button"
              :disabled="activeIndex === 0"
              @click="prevCourse"
            >
              Previous
            </button>

            <span class="course-count">
              {{ activeIndex + 1 }} / {{ courses.length }}
            </span>

            <button
              class="switch-button"
              :disabled="activeIndex === courses.length - 1"
              @click="nextCourse"
            >
              Next
            </button>
          </div>
        </template>

        <p v-else class="state-text">No course content.</p>
      </section>
    </main>
  </div>
</template>

<style scoped>
.page-wrapper {
  min-height: 100vh;
  background: #f7f7f8;
}

.course-page {
  max-width: 1180px;
  margin: 0 auto;
  padding: 80px 24px 64px;
  display: grid;
  grid-template-columns: 280px minmax(0, 1fr);
  gap: 32px;
  align-items: flex-start;
}

.course-sidebar {
  position: sticky;
  top: 88px;
}

.sidebar-inner {
  background: #fff;
  padding: 16px;
}

.sidebar-title {
  font-size: 13px;
  color: #777;
  margin-bottom: 12px;
}

.course-tab {
  width: 100%;
  border: none;
  background: transparent;
  display: grid;
  grid-template-columns: 34px minmax(0, 1fr);
  gap: 10px;
  padding: 12px 10px;
  text-align: left;
  cursor: pointer;
  color: #333;
  transition: all 0.2s ease;
}

.course-tab:hover {
  background: #f2f2f3;
}

.course-tab.active {
  background: #111;
  color: #fff;
}

.course-number {
  font-size: 12px;
  color: inherit;
  opacity: 0.65;
  padding-top: 2px;
}

.course-title {
  font-size: 14px;
  line-height: 1.45;
}

.course-content {
  min-width: 0;
}

.state-text {
  background: #fff;
  padding: 32px;
  color: #555;
}

.error {
  color: #a11313;
}

.markdown-body {
  background: #fff;
  padding: 42px 48px;
  line-height: 1.82;
  color: #222;
  font-size: 15px;
}

.markdown-body :deep(h2) {
  margin: 0 0 22px;
  font-size: 30px;
  line-height: 1.25;
  font-weight: 500;
  color: #111;
  letter-spacing: -0.03em;
}

.markdown-body :deep(h3) {
  margin: 30px 0 10px;
  font-size: 17px;
  font-weight: 600;
  color: #111;
}

.markdown-body :deep(p) {
  margin: 0 0 16px;
  text-align: justify;
}

.markdown-body :deep(p:has(img)) {
  margin: 0 0 28px;
}

.markdown-body :deep(img) {
  width: 220px;
  height: 280px;
  object-fit: cover;
  display: block;
  float: left;
  margin: 4px 32px 18px 0;
  background: #f1f1f1;
}

.markdown-body :deep(strong) {
  font-weight: 600;
  color: #111;
}

.markdown-body :deep(ol) {
  padding-left: 22px;
  margin: 10px 0 18px;
}

.markdown-body :deep(li) {
  margin-bottom: 10px;
}

.markdown-body :deep(li p) {
  margin-bottom: 8px;
}

.course-actions {
  margin-top: 18px;
  background: #fff;
  padding: 16px 18px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.switch-button {
  border: 1px solid #dcdcdc;
  background: #fff;
  color: #111;
  padding: 9px 18px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.switch-button:hover:not(:disabled) {
  background: #111;
  color: #fff;
  border-color: #111;
}

.switch-button:disabled {
  color: #aaa;
  cursor: not-allowed;
  background: #f6f6f6;
}

.course-count {
  font-size: 14px;
  color: #666;
}

@media (max-width: 900px) {
  .course-page {
    display: block;
    padding: 72px 16px 48px;
  }

  .course-sidebar {
    position: static;
    margin-bottom: 18px;
  }

  .sidebar-inner {
    padding: 12px;
  }

  .course-tab {
    grid-template-columns: 30px minmax(0, 1fr);
    padding: 10px 8px;
  }

  .markdown-body {
    padding: 28px 22px;
  }

  .markdown-body :deep(h2) {
    font-size: 24px;
  }

  .markdown-body :deep(img) {
    float: none;
    width: 180px;
    height: 230px;
    margin: 0 0 24px;
  }
}
</style>