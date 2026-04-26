<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter, useRoute, onBeforeRouteLeave } from 'vue-router'

import VMdEditor from '@kangc/v-md-editor'
import '@kangc/v-md-editor/lib/style/base-editor.css'
import githubTheme from '@kangc/v-md-editor/lib/theme/github.js'
import '@kangc/v-md-editor/lib/theme/style/github.css'

import { useApi } from '@/utils/useApi'

VMdEditor.use(githubTheme)

const router = useRouter()
const route = useRoute()

const { data, fetchData, putData, validate, loading, error } = useApi()

const pageKey = computed(() => String(route.params.pageKey || ''))

const title = ref('')
const content = ref('')
const status = ref('PUBLISHED')

const snapshot = ref({
  title: '',
  content: '',
  status: 'PUBLISHED'
})

const isDirty = computed(() => {
  return (
    title.value !== snapshot.value.title ||
    content.value !== snapshot.value.content ||
    status.value !== snapshot.value.status
  )
})

const updateSnapshot = () => {
  snapshot.value = {
    title: title.value,
    content: content.value,
    status: status.value
  }
}

const loadPage = async () => {
  await fetchData(`/api/admin/pages/${pageKey.value}`)

  const page = data.value?.data || data.value

  if (!page) {
    alert('Failed to load page.')
    router.push('/admin')
    return
  }

  title.value = page.title || ''
  content.value = page.content || ''
  status.value = page.status || 'PUBLISHED'

  updateSnapshot()
}

const savePage = async () => {
  if (!title.value.trim()) {
    alert('Please enter a title.')
    return
  }

  if (!content.value.trim()) {
    alert('Please enter content.')
    return
  }

  await putData(`/api/admin/pages/${pageKey.value}`, {
    title: title.value.trim(),
    content: content.value,
    status: status.value
  })

  if (!error.value) {
    updateSnapshot()
    alert('Page saved successfully.')
  }
}

const goBack = () => {
  if (isDirty.value) {
    const ok = window.confirm('You have unsaved changes. Leave anyway?')
    if (!ok) return
  }

  router.push('/admin')
}

const handleUnload = (e: BeforeUnloadEvent) => {
  if (!isDirty.value) return

  e.preventDefault()
  e.returnValue = ''
}

onMounted(async () => {
  const isValid = await validate()

  if (!isValid) {
    router.push('/login')
    return
  }

  await loadPage()

  window.addEventListener('beforeunload', handleUnload)
})

onBeforeUnmount(() => {
  window.removeEventListener('beforeunload', handleUnload)
})

onBeforeRouteLeave(() => {
  if (!isDirty.value) return true
  return window.confirm('You have unsaved changes. Leave anyway?')
})
</script>

<template>
  <div class="editor-page">
    <main class="editor-container">
      <div class="topbar">
        <button class="btn ghost" @click="goBack">
          Back to Admin
        </button>

        <div class="page-key">
          Editing: <strong>{{ pageKey }}</strong>
        </div>
      </div>

      <section class="meta-card">
        <div class="field title-field">
          <label>Title</label>
          <input
            v-model="title"
            placeholder="Enter page title"
          />
        </div>

        <div class="field status-field">
          <label>Status</label>
          <select v-model="status">
            <option value="PUBLISHED">Published</option>
            <option value="DRAFT">Draft</option>
            <option value="HIDDEN">Hidden</option>
          </select>
        </div>
      </section>

      <section class="editor-card">
        <div class="editor-card-head">
          <div>
            <h2>Conference Page Content</h2>
            <p>
              Markdown is supported. This content will be rendered on the public conference page.
            </p>
          </div>

          <span v-if="isDirty" class="status unsaved">Unsaved</span>
          <span v-else class="status saved">Saved</span>
        </div>

        <v-md-editor
          v-model="content"
          height="560px"
          placeholder="Write conference page content in Markdown..."
        />

        <div class="editor-actions">
          <button class="btn solid" :disabled="loading" @click="savePage">
            {{ loading ? 'Saving...' : 'Save Page' }}
          </button>
        </div>

        <p v-if="error" class="error">
          Operation failed. Please try again.
        </p>
      </section>
    </main>
  </div>
</template>

<style scoped>
.editor-page {
  min-height: 100vh;
  background: #f6f6f6;
}

.editor-container {
  max-width: 1180px;
  margin: 0 auto;
  padding: 28px 24px 60px;
}

.topbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-key {
  font-size: 14px;
  color: #666;
}

.btn {
  height: 38px;
  padding: 0 16px;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.ghost {
  border: 1px solid #dcdcdc;
  background: #fff;
  color: #111;
}

.ghost:hover {
  border-color: #111;
}

.solid {
  border: 1px solid #111;
  background: #111;
  color: #fff;
}

.solid:hover {
  opacity: 0.85;
}

.meta-card {
  display: grid;
  grid-template-columns: 1fr 220px;
  gap: 14px;
  margin-bottom: 18px;
}

.field {
  background: #fff;
  border: 1px solid #ddd;
  padding: 14px;
}

.field label {
  display: block;
  margin-bottom: 8px;
  font-size: 12px;
  color: #666;
}

.field input,
.field select {
  width: 100%;
  border: none;
  outline: none;
  font-size: 16px;
  color: #111;
  background: transparent;
}

.editor-card {
  background: #fff;
  border: 1px solid #ddd;
  padding: 16px;
}

.editor-card-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 10px;
}

.editor-card-head h2 {
  margin: 0;
  font-size: 22px;
  color: #111;
}

.editor-card-head p {
  margin: 6px 0 0;
  color: #666;
  font-size: 13px;
}

.status {
  border-radius: 999px;
  padding: 4px 10px;
  font-size: 12px;
}

.status.unsaved {
  background: #fef3c7;
  color: #92400e;
}

.status.saved {
  background: #e8f5e9;
  color: #1b5e20;
}

.editor-actions {
  margin-top: 14px;
  display: flex;
  justify-content: flex-end;
}

.error {
  margin-top: 10px;
  color: #c62828;
  font-size: 13px;
}

@media (max-width: 900px) {
  .meta-card {
    grid-template-columns: 1fr;
  }

  .topbar {
    flex-direction: column;
    align-items: flex-start;
    gap: 14px;
  }
}
</style>