<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useApi } from '@/utils/useApi'

interface PostItem {
  postId: number
  userId: number
  userName: string
  title: string
  content: string
  tag?: string
  status: string
  commentCount: number
  viewCount: number
  isPinned: boolean
}

const emit = defineEmits<{
  (e: 'posts-updated', posts: PostItem[]): void
  (e: 'notify', payload: { type: 'success' | 'error'; text: string }): void
}>()

const { data, fetchData, putData, deleteData } = useApi()

const posts = ref<PostItem[]>([])
const postKeyword = ref('')
const postStatusFilter = ref('ALL')

const filteredPosts = computed(() =>
  posts.value.filter(post => {
    const keyword = postKeyword.value.trim().toLowerCase()

    const hitKeyword =
      !keyword ||
      post.title.toLowerCase().includes(keyword) ||
      post.content.toLowerCase().includes(keyword) ||
      (post.userName || '').toLowerCase().includes(keyword) ||
      (post.tag || '').toLowerCase().includes(keyword)

    const hitStatus =
      postStatusFilter.value === 'ALL' ||
      post.status === postStatusFilter.value

    return hitKeyword && hitStatus
  })
)

const loadPosts = async () => {
  await fetchData('/api/admin/posts')
  posts.value = data.value?.data || data.value || []
  emit('posts-updated', posts.value)
}

const togglePin = async (post: PostItem) => {
  try {
    const nextPinned = !post.isPinned

    await putData(
      `/api/admin/posts/${post.postId}/pin?isPinned=${nextPinned}`,
      {}
    )

    emit('notify', {
      type: 'success',
      text: nextPinned ? 'Post pinned successfully.' : 'Post unpinned successfully.'
    })

    await loadPosts()
  } catch {
    emit('notify', {
      type: 'error',
      text: 'Failed to update pinned status.'
    })
  }
}

const removePost = async (postId: number) => {
  if (!confirm('Delete this post and all related comments?')) return

  try {
    await deleteData(`/api/admin/posts/${postId}`)

    emit('notify', {
      type: 'success',
      text: 'Post deleted successfully.'
    })

    await loadPosts()
  } catch {
    emit('notify', {
      type: 'error',
      text: 'Failed to delete post.'
    })
  }
}

onMounted(loadPosts)

defineExpose({ loadPosts })
</script>

<template>
  <section class="panel">

    <div class="toolbar">
      <input
        v-model="postKeyword"
        placeholder="Search by title, content, tag, or author"
      />

      <select v-model="postStatusFilter">
        <option value="ALL">All Status</option>
        <option value="PUBLISHED">Published</option>
        <option value="DRAFT">Draft</option>
        <option value="HIDDEN">Hidden</option>
      </select>
    </div>

    <table class="data-table">
      <thead>
        <tr>
          <th>ID</th>
          <th>Title</th>
          <th>Author</th>
          <th>Status</th>
          <th>Tag</th>
          <th>Stats</th>
          <th>Pinned</th>
          <th>Actions</th>
        </tr>
      </thead>

      <tbody>
        <tr v-for="post in filteredPosts" :key="post.postId">
          <td>{{ post.postId }}</td>

          <td>
            <strong>{{ post.title }}</strong>
            <span v-if="post.isPinned" class="pin-label">Pinned</span>
          </td>

          <td>
            {{ post.userName || 'Unknown' }}
            <span class="muted">#{{ post.userId }}</span>
          </td>

          <td>{{ post.status }}</td>

          <td>{{ post.tag || '-' }}</td>

          <td>
            {{ post.viewCount }} views ·
            {{ post.commentCount }} comments
          </td>

          <td>
            {{ post.isPinned ? 'Yes' : 'No' }}
          </td>

          <td class="actions">
            <button class="secondary" @click="togglePin(post)">
              {{ post.isPinned ? 'Unpin' : 'Pin' }}
            </button>

            <button class="danger" @click="removePost(post.postId)">
              Delete
            </button>
          </td>
        </tr>

        <tr v-if="filteredPosts.length === 0">
          <td colspan="8" class="empty">
            No posts found.
          </td>
        </tr>
      </tbody>
    </table>
  </section>
</template>

<style scoped>
.panel {
  width: 80%;
  margin: 0 auto;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 18px;
}

.panel h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
}

.panel p {
  margin: 6px 0 0;
  color: #777;
  font-size: 14px;
}

.toolbar {
  display: flex;
  gap: 10px;
  margin-bottom: 14px;
}

.toolbar input {
  flex: 1;
}

input,
select,
button {
  border: 1px solid #ddd;
  border-radius: 6px;
  padding: 8px 10px;
  font-size: 14px;
}

button {
  background: #111;
  color: #fff;
  cursor: pointer;
  transition: 0.15s ease;
}

button:hover {
  opacity: 0.86;
}

button.secondary {
  background: #fff;
  color: #111;
  border-color: #bbb;
}

button.danger {
  background: #b42323;
  border-color: #b42323;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th,
.data-table td {
  border: 1px solid #ececec;
  padding: 9px;
  text-align: left;
  vertical-align: top;
  font-size: 14px;
}

.data-table th {
  background: #fafafa;
  font-weight: 600;
}

.actions {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.pin-label {
  display: inline-block;
  margin-left: 8px;
  padding: 2px 6px;
  border: 1px solid #111;
  border-radius: 4px;
  font-size: 12px;
}

.muted {
  color: #777;
  margin-left: 4px;
}

.empty {
  text-align: center;
  color: #777;
  padding: 24px;
}
</style>