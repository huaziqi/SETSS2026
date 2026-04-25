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

const loadComments = async () => {
  await fetchData(`/api/posts/${props.postId}/comments`)

  if (!error.value && Array.isArray(data.value)) {
    comments.value = data.value
  }
}

const createComment = async (content: string, parentCommentId: number | null = null) => {
  await postData(`/api/posts/${props.postId}/comments`, {
    content,
    parentCommentId
  })

  if (!error.value) {
    await loadComments()
  }
}

const submitComment = async () => {
  const content = commentText.value.trim()
  if (!content) {
    alert('评论内容不能为空')
    return
  }

  await createComment(content, null)

  if (!error.value) {
    commentText.value = ''
  }
}

const submitReply = async (commentId: number, content: string) => {
  await createComment(content, commentId)
}

onMounted(async () => {
  const isValid = await validate()
  if (!isValid) {
    alert('请先登录后评论')
    router.push('/login')
    return
  }

  await loadComments()
})
</script>

<template>
  <section class="comment-section">
    <h2>评论区</h2>

    <div class="publish-box">
      <textarea v-model="commentText" placeholder="写下你的评论..." />
      <button class="submit-btn" :disabled="loading" @click="submitComment">
        {{ loading ? '发布中...' : '发布评论' }}
      </button>
    </div>

    <p v-if="error" class="error">评论操作失败，请重试。</p>

    <div v-if="comments.length" class="comment-list">
      <CommentCard
        v-for="comment in comments"
        :key="comment.commentId"
        :comment="comment"
        @reply="submitReply"
      />
    </div>

    <p v-else class="empty">还没有评论，来抢沙发吧。</p>
  </section>
</template>

<style scoped>
.comment-section {
  margin-top: 28px;
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
  border: 1px solid #d1d5db;
  padding: 10px;
  resize: vertical;
}
.submit-btn {
  margin-top: 10px;
  border: 1px solid #111;
  background: #111;
  color: #fff;
  padding: 8px 16px;
  cursor: pointer;
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
