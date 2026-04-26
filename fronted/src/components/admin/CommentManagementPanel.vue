<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useApi } from '@/utils/useApi'

interface PostItem {
  postId: number
  title: string
}

interface CommentItem {
  commentId: number
  postId: number
  userId: number
  userName: string
  parentCommentId?: number | null
  content: string
  publishTime?: string
}

const props = defineProps<{ posts: PostItem[] }>()

const emit = defineEmits<{
  (e: 'notify', payload: { type: 'success'|'error'; text: string }): void
}>()

const { data, fetchData, deleteData } = useApi()

const comments = ref<CommentItem[]>([])
const commentKeyword = ref('')
const commentPostFilter = ref('ALL')

const filteredComments = computed(() =>
  comments.value.filter(comment => {
    const keyword = commentKeyword.value.trim().toLowerCase()

    const hitKeyword =
      !keyword ||
      comment.content.toLowerCase().includes(keyword) ||
      (comment.userName || '').toLowerCase().includes(keyword)

    const hitPost =
      commentPostFilter.value === 'ALL' ||
      String(comment.postId) === commentPostFilter.value

    return hitKeyword && hitPost
  })
)

const loadComments = async () => {
  await fetchData('/api/admin/comments')
  comments.value = data.value?.data || data.value || []
}

const removeComment = async (commentId: number) => {
  if (!confirm('Delete this comment and all nested replies?')) return

  try {
    await deleteData(`/api/admin/comments/${commentId}`)

    emit('notify', {
      type: 'success',
      text: 'Comment deleted successfully.'
    })

    await loadComments()
  } catch {
    emit('notify', {
      type: 'error',
      text: 'Failed to delete comment.'
    })
  }
}

onMounted(loadComments)

defineExpose({ loadComments })
</script>

<template>
  <section class="panel">
    <div class="toolbar">
      <input
        v-model="commentKeyword"
        placeholder="Search by content or author"
      />

      <select v-model="commentPostFilter">
        <option value="ALL">All Posts</option>

        <option
          v-for="post in posts"
          :key="post.postId"
          :value="String(post.postId)"
        >
          Post #{{ post.postId }} - {{ post.title }}
        </option>
      </select>
    </div>

    <table class="data-table">
      <thead>
        <tr>
          <th>ID</th>
          <th>Post</th>
          <th>Author</th>
          <th>Parent</th>
          <th>Content</th>
          <th>Published</th>
          <th>Actions</th>
        </tr>
      </thead>

      <tbody>
        <tr v-for="comment in filteredComments" :key="comment.commentId">
          <td>{{ comment.commentId }}</td>

          <td>#{{ comment.postId }}</td>

          <td>
            {{ comment.userName }}
            <span class="muted">(#{{ comment.userId }})</span>
          </td>

          <td>{{ comment.parentCommentId || '-' }}</td>

          <td>{{ comment.content }}</td>

          <td>
            {{ comment.publishTime?.replace('T', ' ') || '-' }}
          </td>

          <td>
            <button class="danger" @click="removeComment(comment.commentId)">
              Delete
            </button>
          </td>
        </tr>

        <tr v-if="filteredComments.length === 0">
          <td colspan="7" class="empty">
            No comments found.
          </td>
        </tr>
      </tbody>
    </table>
  </section>
</template>

<style scoped>
.panel {
  background: #fff;
  border: 1px solid #e5e5e5;
  padding: 24px;
}

.panel h2 {
  margin: 0 0 14px;
}

.toolbar {
  display: flex;
  gap: 10px;
  margin-bottom: 14px;
}

.toolbar input {
  flex: 1;
}

input, select, button {
  border: 1px solid #ddd;
  border-radius: 6px;
  padding: 8px;
  font-size: 14px;
}

button {
  background: #111;
  color: #fff;
  cursor: pointer;
}

button.danger {
  background: #b42323;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th,
.data-table td {
  border: 1px solid #ececec;
  padding: 8px;
}

.data-table th {
  background: #fafafa;
}

.muted {
  color: #777;
  margin-left: 4px;
}

.empty {
  text-align: center;
  color: #777;
  padding: 20px;
}
</style>