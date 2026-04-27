<script setup lang="ts">
import { ref } from "vue"
import { useRouter } from "vue-router"
import axios from "axios"

interface SearchResult {
  sourceType: string
  sourceId: number
  pageKey: string | null
  title: string
  url: string
  chunkIndex: number
  anchorId: string
  charStart: number
  charEnd: number
  blockStart: number
  blockEnd: number
  content: string
  distance: number
  similarity: number
}

const router = useRouter()

const keyword = ref("")
const loading = ref(false)
const showDropdown = ref(false)
const results = ref<SearchResult[]>([])

const handleSearch = async () => {
  const text = keyword.value.trim()

  if (!text) {
    results.value = []
    showDropdown.value = false
    return
  }

  loading.value = true
  showDropdown.value = true

  try {
    const res = await axios.post("/api/semantic/search", {
      keyword: text,
      limit: 6
    })

    results.value = res.data
  } catch (e) {
    results.value = []
  } finally {
    loading.value = false
  }
}

const openResult = (item: SearchResult) => {
  showDropdown.value = false
  keyword.value = ""

  router.push({
    path: item.url,
    query: {
      anchorId: item.anchorId,
      blockStart: item.blockStart,
      blockEnd: item.blockEnd
    },
    hash: `#${item.anchorId}`
  })
}

const formatType = (type: string) => {
  if (type === "POST") return "Post"
  if (type === "CONFERENCE_PAGE") return "Page"
  return type
}

const formatSimilarity = (value: number) => {
  return `${Math.round(value * 100)}%`
}

const closeDropdown = () => {
  setTimeout(() => {
    showDropdown.value = false
  }, 160)
}
</script>

<template>
  <div class="semantic-search">
    <div class="search-box">
      <input
        v-model="keyword"
        type="text"
        placeholder="Search..."
        @keyup.enter="handleSearch"
        @focus="keyword && results.length && (showDropdown = true)"
        @blur="closeDropdown"
      />

      <button class="search-button" @click="handleSearch">
        Search
      </button>
    </div>

    <div v-if="showDropdown" class="dropdown">
      <div v-if="loading" class="state">
        Searching...
      </div>

      <template v-else>
        <div
          v-for="item in results"
          :key="item.anchorId"
          class="result-item"
          @click="openResult(item)"
        >
          <div class="result-meta">
            <span>{{ formatType(item.sourceType) }}</span>
            <span>{{ formatSimilarity(item.similarity) }}</span>
          </div>

          <div class="result-title">
            {{ item.title }}
          </div>

          <div class="result-content">
            {{ item.content }}
          </div>
        </div>

        <div v-if="!results.length" class="state">
          No results found.
        </div>
      </template>
    </div>
  </div>
</template>

<style scoped>
.semantic-search {
  position: relative;
  width: 260px;
}

.search-box {
  display: flex;
  align-items: center;
}

.search-box input {
  width: 180px;
  height: 36px;
  padding: 0 12px;
  border: 1px solid #dcdcdc;
  border-right: none;
  border-radius: 6px 0 0 6px;
  outline: none;
  font-size: 14px;
  color: #111;
  box-sizing: border-box;
}

.search-box input:focus {
  border-color: #111;
}

.search-button {
  height: 36px;
  padding: 0 12px;
  border: 1px solid #111;
  border-radius: 0 6px 6px 0;
  background: #111;
  color: #fff;
  font-size: 13px;
  cursor: pointer;
}

.dropdown {
  position: absolute;
  top: 46px;
  right: 0;
  width: 420px;
  max-height: 420px;
  overflow-y: auto;
  background: #fff;
  border: 1px solid #e5e5e5;
  border-radius: 8px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.12);
  z-index: 999;
}

.result-item {
  padding: 14px 16px;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
}

.result-item:hover {
  background: #f8f8f8;
}

.result-meta {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
  font-size: 12px;
  color: #777;
}

.result-title {
  margin-bottom: 6px;
  font-size: 15px;
  font-weight: 600;
  color: #111;
}

.result-content {
  font-size: 13px;
  line-height: 1.5;
  color: #555;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.state {
  padding: 18px;
  font-size: 14px;
  color: #777;
}
</style>