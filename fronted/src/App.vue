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

  // 关键：必须有
  onFinish: () => {
    console.log('AI结束')
  }

}
</script>

<template>
  <div id="app">

    <router-view />

    <SuspendedBallChat
      :url="apiUrl"
      app-name="SETSS2026"
      domain-name="user"
      :enable-streaming="true"
      :callbacks="callbacks"
    />

  </div>
</template>