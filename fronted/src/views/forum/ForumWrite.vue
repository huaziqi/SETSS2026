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
const { postData, putData, fetchData, loading, error, validate, data } = useApi()

const title = ref('')
const tag = ref('')
const content = ref('')
const userInfo = ref<any>(null)

const editingPostId = computed(() => {
  const raw = route.query.postId
  if (!raw) return null
  const id = Number(raw)
  return Number.isNaN(id) ? null : id
})

const isEditMode = computed(() => editingPostId.value !== null)

const snapshot = ref({
  title: '',
  tag: '',
  content: ''
})

const isDirty = computed(() => {
  return (
    title.value !== snapshot.value.title ||
    tag.value !== snapshot.value.tag ||
    content.value !== snapshot.value.content
  )
})

const updateSnapshot = () => {
  snapshot.value = {
    title: title.value,
    tag: tag.value,
    content: content.value
  }
}

const goBack = () => {
  if (isDirty.value) {
    const ok = window.confirm('You have unsaved changes. Leave anyway?')
    if (!ok) return
  }

  router.push('/forum')
}

const saveDraft = async () => {
  if (!title.value.trim() && !content.value.trim()) {
    alert('Title or content cannot be empty')
    return
  }

  const payload = {
    title: title.value,
    tag: tag.value.trim(),
    content: content.value,
    status: 'DRAFT'
  }

  if (isEditMode.value && editingPostId.value) {
    await putData(`/api/posts/${editingPostId.value}`, payload)
  } else {
    await postData('/api/posts', payload)
  }

  if (!error.value) {
    updateSnapshot()
    alert('Draft saved')
  }
}

const publish = async () => {
  if (!title.value.trim()) {
    alert('Please enter a title')
    return
  }

  if (!content.value.trim()) {
    alert('Please enter content')
    return
  }

  const payload = {
    title: title.value,
    tag: tag.value.trim(),
    content: content.value,
    status: 'PUBLISHED'
  }

  if (isEditMode.value && editingPostId.value) {
    await putData(`/api/posts/${editingPostId.value}`, payload)
  } else {
    await postData('/api/posts', payload)
  }

  if (!error.value) {
    updateSnapshot()
    alert(isEditMode.value ? 'Post updated' : 'Post published')
    router.push('/forum/my-posts')
  }
}

const loadPostForEdit = async () => {
  if (!isEditMode.value || !editingPostId.value) {
    return
  }

  await fetchData(`/api/posts/my/${editingPostId.value}`)

  if (error.value || !data.value || data.value.error) {
    alert('Failed to load the post. You may not have access to it.')
    router.push('/forum/my-posts')
    return
  }

  title.value = data.value.title || ''
  tag.value = data.value.tag || ''
  content.value = data.value.content || ''
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

  userInfo.value = data.value

  await loadPostForEdit()

  updateSnapshot()
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
          Back to Forum
        </button>

        <div class="user-box">
          <div class="avatar">
            <svg viewBox="0 0 24 24" fill="none">
              <circle cx="12" cy="8" r="4" stroke="black" stroke-width="2" />
              <path
                d="M4 20c1.5-4 6-6 8-6s6.5 2 8 6"
                stroke="black"
                stroke-width="2"
              />
            </svg>
          </div>

          <span class="username">
            {{ userInfo?.userName || userInfo?.username }}
          </span>
        </div>
      </div>

      <section class="meta-card">
        <div class="field title-field">
          <label>Title</label>
          <input
            v-model="title"
            placeholder="Enter a clear and specific title"
          />
        </div>

        <div class="field tag-field">
          <label>Tag</label>
          <input
            v-model="tag"
            placeholder="e.g. Schedule, Submission, Travel"
          />
        </div>
      </section>

      <section class="editor-card">
        <div class="editor-card-head">
          <div>
            <h2>{{ isEditMode ? 'Edit Content' : 'Content' }}</h2>
            <p>
              Markdown is supported. You can use headings, tables, code blocks,
              and links.
            </p>
          </div>

          <span v-if="isDirty" class="status unsaved">Unsaved</span>
          <span v-else class="status saved">Saved</span>
        </div>

        <v-md-editor
          v-model="content"
          height="560px"
          placeholder="Write your content in Markdown..."
        />

        <div class="editor-actions">
          <button class="btn outline" :disabled="loading" @click="saveDraft">
            {{ loading ? 'Saving...' : 'Save Draft' }}
          </button>

          <button class="btn solid" :disabled="loading" @click="publish">
            {{ loading ? (isEditMode ? 'Updating...' : 'Publishing...') : (isEditMode ? 'Update & Publish' : 'Publish') }}
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

.btn {
  height: 38px;
  padding: 0 16px;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
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

.outline {
  border: 1px solid #111;
  background: #fff;
  color: #111;
}

.outline:hover {
  background: #111;
  color: #fff;
}

.solid {
  border: 1px solid #111;
  background: #111;
  color: #fff;
}

.solid:hover {
  opacity: 0.85;
}

.user-box {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.user-box:hover {
  opacity: 0.7;
}

.avatar {
  width: 30px;
  height: 30px;
  border: 1px solid #111;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar svg {
  width: 16px;
  height: 16px;
}

.username {
  font-size: 15px;
  max-width: 120px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.meta-card {
  display: grid;
  grid-template-columns: 1fr 260px;
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

.field input {
  width: 100%;
  border: none;
  outline: none;
  font-size: 16px;
  color: #111;
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
  gap: 10px;
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
