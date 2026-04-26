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
const emit = defineEmits<{ (e: 'notify', payload: { type: 'success'|'error'; text: string }): void }>()
const { data, fetchData, postData, putData, deleteData } = useApi()

const comments = ref<CommentItem[]>([])
const commentKeyword = ref('')
const commentPostFilter = ref('ALL')

const commentForm = ref({
  commentId: null as number | null,
  postId: '',
  userId: '',
  parentCommentId: '',
  content: ''
})

const filteredComments = computed(() =>
  comments.value.filter(comment => {
    const keyword = commentKeyword.value.trim().toLowerCase()
    const hitKeyword = !keyword ||
      comment.content.toLowerCase().includes(keyword) ||
      (comment.userName || '').toLowerCase().includes(keyword)
    const hitPost = commentPostFilter.value === 'ALL' || String(comment.postId) === commentPostFilter.value
    return hitKeyword && hitPost
  })
)

const loadComments = async () => {
  await fetchData('/admin/comments')
  comments.value = data.value?.data || []
}

const resetCommentForm = () => {
  commentForm.value = {
    commentId: null,
    postId: '',
    userId: '',
    parentCommentId: '',
    content: ''
  }
}

const saveComment = async () => {
  try {
    const payload = {
      parentCommentId: commentForm.value.parentCommentId ? Number(commentForm.value.parentCommentId) : null,
      content: commentForm.value.content
    }

    if (commentForm.value.commentId) {
      await putData(`/admin/comments/${commentForm.value.commentId}`, payload)
      emit('notify', { type: 'success', text: 'Comment updated successfully.' })
    } else {
      await postData(`/admin/comments?postId=${Number(commentForm.value.postId)}&userId=${Number(commentForm.value.userId)}`, payload)
      emit('notify', { type: 'success', text: 'Comment created successfully.' })
    }

    resetCommentForm()
    await loadComments()
  } catch {
    emit('notify', { type: 'error', text: 'Failed to save comment.' })
  }
}

const editComment = (comment: CommentItem) => {
  commentForm.value = {
    commentId: comment.commentId,
    postId: String(comment.postId),
    userId: String(comment.userId),
    parentCommentId: comment.parentCommentId ? String(comment.parentCommentId) : '',
    content: comment.content
  }
}

const removeComment = async (commentId: number) => {
  if (!confirm('Delete this comment and all nested replies?')) return
  await deleteData(`/admin/comments/${commentId}`)
  emit('notify', { type: 'success', text: 'Comment deleted successfully.' })
  await loadComments()
}

onMounted(loadComments)
defineExpose({ loadComments })
</script>

<template>
  <section class="panel">
    <h2>Comment Management</h2>
    <div class="toolbar">
      <input v-model="commentKeyword" placeholder="Search by content or author" />
      <select v-model="commentPostFilter">
        <option value="ALL">All Posts</option>
        <option v-for="post in posts" :key="post.postId" :value="String(post.postId)">
          Post #{{ post.postId }} - {{ post.title }}
        </option>
      </select>
      <button @click="resetCommentForm">New Comment</button>
    </div>

    <div class="form-grid">
      <input v-model="commentForm.postId" placeholder="Post ID" />
      <input v-model="commentForm.userId" placeholder="Author User ID" />
      <input v-model="commentForm.parentCommentId" placeholder="Parent Comment ID (optional)" :disabled="!!commentForm.commentId" />
      <textarea v-model="commentForm.content" rows="3" placeholder="Comment content"></textarea>
      <button @click="saveComment">{{ commentForm.commentId ? 'Update Comment' : 'Create Comment' }}</button>
    </div>

    <table class="data-table">
      <thead>
        <tr>
          <th>ID</th><th>Post</th><th>Author</th><th>Parent</th><th>Content</th><th>Published</th><th>Actions</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="comment in filteredComments" :key="comment.commentId">
          <td>{{ comment.commentId }}</td>
          <td>#{{ comment.postId }}</td>
          <td>{{ comment.userName }} (#{{ comment.userId }})</td>
          <td>{{ comment.parentCommentId || '-' }}</td>
          <td>{{ comment.content }}</td>
          <td>{{ comment.publishTime?.replace('T', ' ') || '-' }}</td>
          <td>
            <button @click="editComment(comment)">Edit</button>
            <button class="danger" @click="removeComment(comment.commentId)">Delete</button>
          </td>
        </tr>
      </tbody>
    </table>
  </section>
</template>

<style scoped>
.panel { background: #fff; border: 1px solid #e5e5e5; padding: 24px; }
.panel h2 { margin: 0 0 14px; }
.toolbar { display: flex; gap: 10px; margin-bottom: 14px; }
.toolbar input, .toolbar select, .toolbar button { height: 36px; }
.form-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 10px; margin-bottom: 14px; }
textarea { grid-column: 1 / -1; }
input, select, textarea, button { border: 1px solid #ddd; border-radius: 6px; padding: 8px; font-size: 14px; }
button { background: #111; color: #fff; cursor: pointer; }
button.danger { background: #b42323; border-color: #b42323; }
.data-table { width: 100%; border-collapse: collapse; }
.data-table th, .data-table td { border: 1px solid #ececec; padding: 8px; text-align: left; vertical-align: top; }
.data-table th { background: #fafafa; }
</style>
