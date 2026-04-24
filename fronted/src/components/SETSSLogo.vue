<script setup lang="ts">
import { useRouter } from 'vue-router'

const props = defineProps<{
  size?: 'small' | 'medium' | 'large' | 'xlarge'
  subtitle?: string
}>()

const router = useRouter()

const goHome = () => {
  router.push('/')
}
</script>

<template>
  <div class="logo-wrapper">
    <div class="logo" :class="props.size || 'large'" @click="goHome">
      <div class="main-text">
        <span class="logo-s">S</span>ETSS2026
      </div>
      <p v-if="subtitle" class="sub-text">{{ subtitle }}</p>
    </div>
  </div>
</template>

<style scoped>
/* 整体居中 */
.logo-wrapper {
  width: 100%;
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}

/* logo本体 */
.logo {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  cursor: pointer;
  user-select: none;
}

/* 点击反馈 */
.logo:hover {
  opacity: 0.8;
  transform: translateY(-2px);
  transition: all 0.2s ease;
}

.main-text {
  font-weight: 800;
  letter-spacing: 2px;
  color: #111;
}

/* 放大的 S */
.logo-s {
  font-weight: 900;
  margin-right: 2px;
}

/* subtitle */
.sub-text {
  margin-top: 6px;
  font-size: 13px;
  color: #666;
  letter-spacing: 1px;
}

/* 尺寸控制 */
.logo.small .main-text {
  font-size: 22px;
}
.logo.small .logo-s {
  font-size: 30px;
}

.logo.medium .main-text {
  font-size: 30px;
}
.logo.medium .logo-s {
  font-size: 42px;
}

.logo.large .main-text {
  font-size: 42px;
}
.logo.large .logo-s {
  font-size: 56px;
}

/* 🔥 新增更大尺寸 */
.logo.xlarge .main-text {
  font-size: 52px;
}
.logo.xlarge .logo-s {
  font-size: 70px;
}
</style>