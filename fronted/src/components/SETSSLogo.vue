<script setup lang="ts">
defineProps<{
  size?: 'small' | 'medium' | 'large'
  subtitle?: string
}>()
</script>

<template>
  <div class="logo" :class="size || 'medium'">
    <div class="main-text">
      <span class="logo-s">S</span>ETSS2026
    </div>
    <p v-if="subtitle" class="sub-text">{{ subtitle }}</p>
  </div>
</template>

<style scoped>
.logo {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.main-text {
  font-weight: 800;
  letter-spacing: 2px;
  color: #111;
}

/* 重点：放大的 S */
.logo-s {
  font-weight: 900;
  margin-right: 1px;
}

/* subtitle */
.sub-text {
  margin-top: 6px;
  font-size: 13px;
  color: #666;
  letter-spacing: 1px;
}

/* small */
.logo.small .main-text {
  font-size: 22px;
}
.logo.small .logo-s {
  font-size: 28px;
}

/* medium */
.logo.medium .main-text {
  font-size: 30px;
}
.logo.medium .logo-s {
  font-size: 38px;
}

/* large（推荐你用这个） */
.logo.large .main-text {
  font-size: 38px;
}
.logo.large .logo-s {
  font-size: 50px;
}

.logo.large .sub-text {
  font-size: 14px;
}
</style>