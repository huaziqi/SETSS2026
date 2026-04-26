<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useApi } from '@/utils/useApi'
import CommentCard, { type CommentItem } from './CommentCard.vue'

const props = defineProps<{
  postId: number
}>()

const router = useRouter()
const { fetchData, postData, data, error, loading, validate } = useApi()

const comments = ref<CommentItem[]>([])
const commentText = ref('')
const isLogin = ref(false)

const loadComments = async () => {
  await fetchData(`/api/posts/${props.postId}/comments`)

  if (!error.value && Array.isArray(data.value)) {
    comments.value = data.value
  }
}

const createComment = async (
  content: string,
  parentCommentId: number | null = null
) => {
  await postData(`/api/posts/${props.postId}/comments`, {
    content,
    parentCommentId
  })

  if (!error.value) {
    await loadComments()
  }
}

const submitComment = async () => {
  if (!isLogin.value) {
    router.push('/login')
    return
  }

  const content = commentText.value.trim()

  if (!content) {
    alert('Comment cannot be empty')
    return
  }

  await createComment(content, null)

  if (!error.value) {
    commentText.value = ''
  }
}

const submitReply = async (commentId: number, content: string) => {
  if (!isLogin.value) {
    router.push('/login')
    return
  }

  await createComment(content, commentId)
}

onMounted(async () => {
  const isValid = await validate()
  isLogin.value = isValid

  await loadComments()
})
</script>

<template>
  <section class="comment-section">
    <h2>Comments</h2>

    <div v-if="isLogin" class="publish-box">
      <textarea
        v-model="commentText"
        placeholder="Write your comment..."
      />

      <button
        class="submit-btn"
        :disabled="loading"
        @click="submitComment"
      >
        {{ loading ? 'Posting...' : 'Post Comment' }}
      </button>
    </div>

    <div v-else class="login-tip">
      Please
      <span @click="router.push('/login')">log in</span>
      to post a comment.
    </div>

    <p v-if="error" class="error">
      Comment operation failed. Please try again.
    </p>

    <div v-if="comments.length" class="comment-list">
      <CommentCard
        v-for="comment in comments"
        :key="comment.commentId"
        :comment="comment"
        @reply="submitReply"
      />
    </div>

    <p v-else class="empty">
      No comments yet. Be the first to comment.
    </p>
  </section>
</template>

<style scoped>
.comment-section {
  margin-top: 28px;
}

.comment-section h2 {
  margin: 0 0 14px;
  font-size: 22px;
  color: #111;
}

.publish-box {
  border: 1px solid #e5e7eb;
  background: #fff;
  padding: 14px;
  margin-bottom: 16px;
}

.publish-box textarea {
  width: 100%;
  min-height: 100px;
  box-sizing: border-box;
  border: 1px solid #d1d5db;
  padding: 10px;
  resize: vertical;
  outline: none;
  font-family: inherit;
  font-size: 14px;
}

.publish-box textarea:focus {
  border-color: #111;
}

.submit-btn {
  margin-top: 10px;
  border: 1px solid #111;
  background: #111;
  color: #fff;
  padding: 8px 16px;
  cursor: pointer;
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.login-tip {
  padding: 14px;
  border: 1px dashed #d1d5db;
  background: #fafafa;
  color: #666;
  font-size: 14px;
  margin-bottom: 16px;
}

.login-tip span {
  color: #111;
  font-weight: 500;
  cursor: pointer;
}

.login-tip span:hover {
  text-decoration: underline;
}

.comment-list {
  margin-top: 10px;
}

.error {
  color: #dc2626;
}

.empty {
  color: #6b7280;
}
</style>