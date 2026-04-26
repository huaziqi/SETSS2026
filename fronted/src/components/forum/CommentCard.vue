<script setup lang="ts">
import { computed, ref } from 'vue'

defineOptions({ name: 'CommentCard' })

export type CommentItem = {
  commentId: number
  postId: number
  userId: number
  userName: string
  parentCommentId?: number | null
  content: string
  publishTime: string
  replies: CommentItem[]
}

const props = defineProps<{
  comment: CommentItem
  depth?: number
}>()

const emit = defineEmits<{
  (e: 'reply', commentId: number, content: string): void
}>()

const showReplyBox = ref(false)
const replyText = ref('')

const currentDepth = computed(() => props.depth ?? 0)

const submitReply = () => {
  const text = replyText.value.trim()
  if (!text) return

  emit('reply', props.comment.commentId, text)
  replyText.value = ''
  showReplyBox.value = false
}

const formatTime = (time: string) => {
  return new Date(time).toLocaleString()
}
</script>

<template>
  <article class="comment-card" :style="{ marginLeft: `${Math.min(currentDepth * 20, 60)}px` }">
    <header class="comment-head">
      <strong>{{ comment.userName }}</strong>
      <span>{{ formatTime(comment.publishTime) }}</span>
    </header>

    <p class="comment-content">{{ comment.content }}</p>

    <button class="reply-btn" @click="showReplyBox = !showReplyBox">
      {{ showReplyBox ? '取消回复' : '回复' }}
    </button>

    <div v-if="showReplyBox" class="reply-editor">
      <textarea
        v-model="replyText"
        placeholder="写下你的回复..."
      />
      <button class="send-btn" @click="submitReply">发布回复</button>
    </div>

    <div v-if="comment.replies?.length" class="reply-list">
      <CommentCard
        v-for="reply in comment.replies"
        :key="reply.commentId"
        :comment="reply"
        :depth="currentDepth + 1"
        @reply="(commentId, content) => emit('reply', commentId, content)"
      />
    </div>
  </article>
</template>

<style scoped>
.comment-card {
  border: 1px solid #e5e7eb;
  background: #fff;
  padding: 14px;
  margin-bottom: 12px;
}
.comment-head {
  display: flex;
  justify-content: space-between;
  color: #555;
  font-size: 13px;
}
.comment-content {
  margin: 10px 0;
  white-space: pre-wrap;
  line-height: 1.6;
}
.reply-btn {
  border: none;
  background: transparent;
  color: #2563eb;
  cursor: pointer;
  padding: 0;
}
.reply-editor {
  margin-top: 10px;
}
.reply-editor textarea {
  width: 100%;
  min-height: 80px;
  border: 1px solid #d1d5db;
  padding: 10px;
  resize: vertical;
}
.send-btn {
  margin-top: 8px;
  border: 1px solid #111;
  background: #111;
  color: #fff;
  padding: 6px 12px;
  cursor: pointer;
}
.reply-list {
  margin-top: 12px;
}
</style>
