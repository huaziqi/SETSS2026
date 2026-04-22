<script setup lang="ts">
const props = withDefaults(defineProps<{
  mode?: 'light' | 'dark'
  size?: 'small' | 'medium' | 'large'
}>(), {
  mode: 'light',
  size: 'medium'
})
</script>

<template>
  <button class="btn" :class="[props.mode, props.size]">
    <slot></slot>
  </button>
</template>

<style scoped>
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;

  /* border-radius: 10px;
  cursor: pointer;
  transition: all 0.25s ease; */

  font-weight: 500;
  letter-spacing: 0.5px;
  white-space: nowrap;

  min-width: 1.618em;
  border: 1.5px solid black;

  background: white;
  color: black;
}

/* ===== 尺寸 ===== */
.small {
  height: 32px;
  padding: 0 16px;
  font-size: 12px;
  min-width: 52px;
}

.medium {
  height: 40px;
  padding: 0 22px;
  font-size: 14px;
  min-width: 65px;
}

.large {
  height: 52px;
  padding: 0 30px;
  font-size: 16px;
  min-width: 84px;
}

/* ===== 白天模式 ===== */
.light {
  background: white;
  color: black;
  border: 1.5px solid black;
}

.light:hover {
  background: black;
  color: white;
}

/* ===== 黑暗模式 ===== */
.dark {
  background: black;
  color: white;
  border: 1.5px solid black;
}

.dark:hover {
  background: white;
  color: black;
}

/* 点击反馈 */
.btn:active {
  transform: scale(0.97);
}
</style>