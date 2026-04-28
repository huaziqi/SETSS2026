<script setup lang="ts">
import Header from '@/layouts/Header.vue'
import { computed, onMounted, ref } from 'vue'
import { useApi } from '@/utils/useApi'

const { data, fetchData, loading, error } = useApi()

const page = computed(() => data.value?.data || data.value || null)

const htmlContent = computed(() => {
  return page.value?.htmlContent || ''
})

const years = ['2017', '2018', '2019', '2024', '2025']
const activeYear = ref('2017')
const activeIndex = ref(0)

const imageModules = import.meta.glob(
  '@/assets/SETSS2026_images_by_year_category/*/Overview/*.{png,jpg,jpeg,webp}',
  {
    eager: true,
    query: '?url',
    import: 'default'
  }
)

const overviewImages = computed<Record<string, string[]>>(() => {
  const result: Record<string, string[]> = {}

  years.forEach(year => {
    result[year] = []
  })

  Object.entries(imageModules).forEach(([path, url]) => {
    const matched = path.match(/SETSS2026_images_by_year_category\/(\d{4})\/Overview\//)

    if (matched) {
      const year = matched[1]
      if (!result[year]) result[year] = []
      result[year].push(url as string)
    }
  })

  Object.keys(result).forEach(year => {
    result[year].sort()
  })

  return result
})

const allOverviewImages = computed(() => {
  return years.flatMap(year =>
    (overviewImages.value[year] || []).map(url => ({
      year,
      url
    }))
  )
})

const currentItem = computed(() => {
  return allOverviewImages.value[activeIndex.value] || null
})

const currentImage = computed(() => {
  return currentItem.value?.url || ''
})

const currentYear = computed(() => {
  return currentItem.value?.year || activeYear.value
})

const prevImage = () => {
  if (!allOverviewImages.value.length) return

  activeIndex.value =
    activeIndex.value === 0
      ? allOverviewImages.value.length - 1
      : activeIndex.value - 1
}

const nextImage = () => {
  if (!allOverviewImages.value.length) return

  activeIndex.value =
    activeIndex.value === allOverviewImages.value.length - 1
      ? 0
      : activeIndex.value + 1
}

onMounted(() => {
  fetchData('/api/admin/pages/home')
})
</script>

<template>
  <div class="page-wrapper">
    <Header />

    <main class="home">
      <section class="overview-gallery">
        <div v-if="currentImage" class="image-stage">
          <div
            class="blur-bg"
            :style="{ backgroundImage: `url(${currentImage})` }"
          ></div>

          <button class="arrow left" @click="prevImage">‹</button>

          <img
            class="main-image"
            :src="currentImage"
            :alt="`SETSS ${currentYear} Overview`"
          />

          <button class="arrow right" @click="nextImage">›</button>
        </div>

        <div v-else class="empty-gallery">
          No overview images.
        </div>

        <div v-if="allOverviewImages.length > 1" class="gallery-footer">
          <span>
            SETSS {{ currentYear }} · {{ activeIndex + 1 }} / {{ allOverviewImages.length }}
          </span>

          <div class="dots">
            <button
              v-for="(_, index) in allOverviewImages"
              :key="index"
              class="dot"
              :class="{ active: activeIndex === index }"
              @click="activeIndex = index"
            ></button>
          </div>
        </div>
      </section>

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
  background: #fafafa;
}

.home {
  padding-top: 64px;
  max-width: 1120px;
  margin: 0 auto;
  padding-left: 24px;
  padding-right: 24px;
}

.overview-gallery {
  margin-top: 0;
  margin-bottom: 24px;
  padding: 0;
  background: transparent;
}

.image-stage {
  position: relative;
  height: 480px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
}

.blur-bg {
  position: absolute;
  inset: 0;
  background-size: cover;
  background-position: center;
  filter: blur(30px);
  transform: scale(1.2);
  opacity: 0.62;
}

.blur-bg::after {
  content: "";
  position: absolute;
  inset: 0;
  background: linear-gradient(
    to right,
    rgba(250, 250, 250, 0.92) 0%,
    rgba(250, 250, 250, 0.45) 20%,
    rgba(250, 250, 250, 0.08) 40%,
    rgba(250, 250, 250, 0.08) 60%,
    rgba(250, 250, 250, 0.45) 80%,
    rgba(250, 250, 250, 0.92) 100%
  );
  backdrop-filter: blur(12px);
}

.main-image {
  position: relative;
  z-index: 2;
  max-width: 70%;
  max-height: 100%;
  object-fit: contain;
  background: #fff;
  box-shadow:
    0 30px 80px rgba(0, 0, 0, 0.25),
    0 10px 30px rgba(0, 0, 0, 0.15);
}

.arrow {
  position: absolute;
  z-index: 3;
  top: 50%;
  transform: translateY(-50%);
  width: 42px;
  height: 72px;
  border: none;
  background: rgba(255, 255, 255, 0.68);
  color: #111;
  font-size: 40px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.arrow:hover {
  background: #fff;
}

.arrow.left {
  left: 18px;
}

.arrow.right {
  right: 18px;
}

.gallery-footer {
  margin-top: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #666;
  font-size: 14px;
}

.dots {
  display: flex;
  gap: 7px;
  flex-wrap: wrap;
  justify-content: flex-end;
  max-width: 70%;
}

.dot {
  width: 7px;
  height: 7px;
  border: none;
  border-radius: 50%;
  background: #cfcfcf;
  cursor: pointer;
  padding: 0;
}

.dot.active {
  background: #111;
}

.empty-gallery {
  height: 220px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #777;
  background: #fff;
}

.content {
  background: #fff;
  padding: 36px 44px;
  margin-bottom: 60px;
}

.markdown-body {
  line-height: 1.85;
  font-size: 16px;
  color: #222;
}

.markdown-body :deep(img) {
  max-width: 100%;
  height: auto;
  display: block;
  margin: 24px auto;
}

.markdown-body :deep(.md-block) {
  margin-bottom: 8px;
}

.markdown-body :deep(h1),
.markdown-body :deep(h2),
.markdown-body :deep(h3) {
  margin-top: 1.3em;
  margin-bottom: 0.7em;
  font-weight: 500;
}

.markdown-body :deep(p) {
  margin: 0.7em 0;
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

@media (max-width: 768px) {
  .home {
    padding-top: 56px;
    padding-left: 14px;
    padding-right: 14px;
  }

  .image-stage {
    height: 300px;
  }

  .main-image {
    max-width: 86%;
  }

  .arrow {
    width: 34px;
    height: 56px;
    font-size: 32px;
  }

  .gallery-footer {
    align-items: flex-start;
    gap: 10px;
    flex-direction: column;
  }

  .dots {
    max-width: 100%;
    justify-content: flex-start;
  }

  .content {
    padding: 24px 20px;
  }
}
</style>