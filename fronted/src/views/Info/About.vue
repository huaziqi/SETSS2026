<script setup lang="ts">
import Header from '@/layouts/Header.vue'
import { computed, onMounted, ref } from 'vue'
import { marked } from 'marked'
import { useApi } from '@/utils/useApi'

const { data, fetchData, loading, error } = useApi()

const page = computed(() => data.value?.data || data.value || null)

const htmlContent = computed(() => {
  return page.value?.content ? marked.parse(page.value.content) : ''
})

const years = ['2017', '2018', '2019']
const activeYear = ref('2017')

const imageModules = import.meta.glob(
  '@/assets/SETSS2026_images_by_year_category/*/*/*.{png,jpg,jpeg,webp}',
  {
    eager: true,
    query: '?url',
    import: 'default'
  }
)

const momentImages = computed(() => {
  const result: Record<string, { course: string[]; social: string[] }> = {}

  years.forEach(year => {
    result[year] = {
      course: [],
      social: []
    }
  })

  Object.entries(imageModules).forEach(([path, url]) => {
    const matched = path.match(
      /SETSS2026_images_by_year_category\/(\d{4})\/([^/]+)\//
    )

    if (!matched) return

    const year = matched[1]
    const category = matched[2]

    if (!result[year]) return

    const normalizedCategory = category.toLowerCase().replace(/_/g, '')

    if (normalizedCategory === 'coursemoments') {
      result[year].course.push(url as string)
    }

    if (normalizedCategory === 'socialmoments') {
      result[year].social.push(url as string)
    }
  })

  Object.keys(result).forEach(year => {
    result[year].course.sort()
    result[year].social.sort()
  })

  return result
})

const currentImages = computed(() => {
  return momentImages.value[activeYear.value]
})

const selectYear = (year: string) => {
  activeYear.value = year
}

onMounted(() => {
  fetchData('/api/admin/pages/about')
})
</script>

<template>
  <div class="page-wrapper">
    <Header />

    <main class="page">
      <section class="about-content">
        <h1>{{ page?.title || 'About' }}</h1>

        <p v-if="loading">Loading...</p>
        <p v-else-if="error" class="error">Failed to load</p>

        <div v-else v-html="htmlContent" class="markdown-body"></div>
      </section>

      <section class="moments-section">
        <div class="moments-header">
          <div>
            <h2>SETSS Moments</h2>
          </div>

          <div class="year-nav">
            <button
              v-for="year in years"
              :key="year"
              class="year-button"
              :class="{ active: activeYear === year }"
              @click="selectYear(year)"
            >
              {{ year }}
            </button>
          </div>
        </div>

        <div class="year-block">
          <h3>{{ activeYear }}</h3>

          <div class="moment-group">
            <div class="group-title">
              Course Moments
            </div>

            <div
              v-if="currentImages?.course.length"
              class="image-grid"
            >
              <img
                v-for="(img, index) in currentImages.course"
                :key="`course-${activeYear}-${index}`"
                :src="img"
                :alt="`SETSS ${activeYear} Course Moment ${index + 1}`"
              />
            </div>

            <p v-else class="empty-text">
              No course moment images.
            </p>
          </div>

          <div class="moment-group">
            <div class="group-title">
              Social Moments
            </div>

            <div
              v-if="currentImages?.social.length"
              class="image-grid"
            >
              <img
                v-for="(img, index) in currentImages.social"
                :key="`social-${activeYear}-${index}`"
                :src="img"
                :alt="`SETSS ${activeYear} Social Moment ${index + 1}`"
              />
            </div>

            <p v-else class="empty-text">
              No social moment images.
            </p>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>

<style scoped>
.page-wrapper {
  min-height: 100vh;
  background: #f7f7f8;
}

.page {
  padding: 80px 24px 72px;
  max-width: 1100px;
  margin: 0 auto;
}

.about-content {
  background: #fff;
  padding: 40px 48px;
  margin-bottom: 36px;
}

.about-content h1 {
  margin: 0 0 28px;
  font-size: 34px;
  font-weight: 500;
  color: #111;
  letter-spacing: -0.03em;
}

.markdown-body {
  line-height: 1.85;
  color: #222;
  font-size: 15px;
}

.markdown-body :deep(p) {
  margin: 0 0 16px;
  text-align: justify;
}

.markdown-body :deep(h2) {
  margin: 34px 0 14px;
  font-size: 24px;
  font-weight: 500;
  color: #111;
}

.markdown-body :deep(h3) {
  margin: 28px 0 12px;
  font-size: 18px;
  font-weight: 600;
  color: #111;
}

.error {
  color: #a11313;
}

.moments-section {
  background: #fff;
  padding: 40px 48px;
}

.moments-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 24px;
  margin-bottom: 32px;
}

.moments-header h2 {
  margin: 0;
  font-size: 30px;
  font-weight: 500;
  color: #111;
  letter-spacing: -0.03em;
}

.moments-header p {
  margin: 8px 0 0;
  color: #666;
  font-size: 15px;
}

.year-nav {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.year-button {
  border: 1px solid #dcdcdc;
  background: #fff;
  color: #222;
  padding: 8px 16px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.year-button:hover,
.year-button.active {
  background: #111;
  color: #fff;
  border-color: #111;
}

.year-block {
  padding-top: 4px;
}

.year-block h3 {
  margin: 0 0 24px;
  font-size: 24px;
  font-weight: 500;
  color: #111;
}

.moment-group {
  margin-bottom: 34px;
}

.moment-group:last-child {
  margin-bottom: 0;
}

.group-title {
  margin-bottom: 14px;
  font-size: 15px;
  font-weight: 600;
  color: #222;
}

.image-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
}

.image-grid img {
  width: 100%;
  height: 180px;
  object-fit: cover;
  display: block;
  background: #f1f1f1;
}

.empty-text {
  margin: 0;
  color: #888;
  font-size: 14px;
}

@media (max-width: 900px) {
  .page {
    padding: 72px 16px 48px;
  }

  .about-content,
  .moments-section {
    padding: 28px 22px;
  }

  .moments-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .image-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .image-grid img {
    height: 150px;
  }
}

@media (max-width: 520px) {
  .image-grid {
    grid-template-columns: 1fr;
  }

  .image-grid img {
    height: 210px;
  }
}
</style>