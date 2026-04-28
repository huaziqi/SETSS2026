<script setup lang="ts">
import { SuspendedBallChat } from 'ai-suspended-ball-chat'

const apiUrl = window.location.origin + '/api/ai/chat'

const callbacks = {
  onAssistantMessage: (msg: any) => {
    console.log('AI回复:', msg)
  },
  onError: (err: any) => {
    console.error('AI错误:', err)
  },
  onFinish: () => {
    console.log('AI结束')
  }
}
</script>

<template>
  <div id="app">
    <router-view />

    <SuspendedBallChat
  class="minimalist-chat"
  :url="apiUrl"
  app-name="SETSS2026"
  domain-name="user"

  title="SETSS2026 智能助手"

  :enable-streaming="true"
  :callbacks="callbacks"
/>
  </div>
</template>

<style scoped>

/* =========================
   悬浮球（一定生效版）
   ========================= */

:deep(.minimalist-chat .chat-bubble) {

  background: #000 !important;

  border-radius: 50% !important;

  border: 1px solid #333 !important;

  box-shadow:
    0 4px 12px rgba(0,0,0,0.2) !important;

  transition: all 0.2s ease !important;
}



:deep(.minimalist-chat .chat-bubble:hover) {

  background: #111 !important;

  transform: scale(1.08) !important;

}



/* icon */

:deep(.minimalist-chat .chat-icon svg) {

  color: #fff !important;

}



/* =========================
   聊天窗口
   ========================= */

:deep(.minimalist-chat .chat-panel) {

  background: #ffffff !important;

  border-radius: 14px !important;

  border: 1px solid #e5e5e5 !important;

  box-shadow:
    0 8px 24px rgba(0,0,0,0.08) !important;

}



/* header */

:deep(.minimalist-chat .chat-header) {

  background: #ffffff !important;

  border-bottom: 1px solid #f0f0f0 !important;

  font-weight: 600 !important;

}

:deep(.chat-header) {
  font-size: 0;
}

:deep(.chat-header::after) {
  font-size: 16px;
  font-weight: 600;
  color: #000;
}



/* 用户消息 */

:deep(.minimalist-chat .message-user) {

  background: #000 !important;

  color: #fff !important;

}



/* AI 消息 */

:deep(.minimalist-chat .message-assistant) {

  background: #f5f5f5 !important;

  color: #333 !important;

}



/* 输入框 */

:deep(.minimalist-chat .chat-input) {

  background: #f9f9f9 !important;

  border-radius: 8px !important;

  border: 1px solid transparent !important;

}



:deep(.minimalist-chat .chat-input:focus) {

  border-color: #000 !important;

}



/* 发送按钮 */

:deep(.minimalist-chat .send-btn) {

  background: transparent !important;

}



:deep(.minimalist-chat .send-btn:hover) {

  background: #f0f0f0 !important;

}

</style>