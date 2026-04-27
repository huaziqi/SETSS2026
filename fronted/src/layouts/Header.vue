<script setup lang="ts">
import BaseButton from "../components/BaseButton.vue";
import SemanticSearchBox from "../components/semantic/SemanticSearchBox.vue"
import { ref, onMounted } from "vue";
import { useApi } from "../utils/useApi";

import { useRouter, useRoute } from "vue-router";

const router = useRouter();
const route = useRoute();

const handleClick = (path: string) => {
  router.push(path);
};

const isLogin = ref(false);
const active = ref("home");
const userName = ref("");
const { validate, data } = useApi();

onMounted(async () => {
  const isValid = await validate();
  if (isValid) {
    userName.value = data.value.userName;
    isLogin.value = true;
  }
});

const menu = [
  { key: "home", label: "Home", path: "/" },
  { key: "schedule", label: "Schedule", path: "/schedule" },
  { key: "courses", label: "Courses", path: "/courses" },
  { key: "participation", label: "Participation", path: "/participation" },
  { key: "manuscript", label: "Manuscript", path: "/submit" }, // 新增稿件提交
  { key: "about", label: "About", path: "/about" },
  { key: "forum", label: "Forum", path: "/forum" },
];
</script>

<template>
  <header class="header">
    <!-- Logo 部分，点击可回家 -->
    <div class="logo">SETSS 2026</div>

    <nav class="nav">
      <div
        v-for="item in menu"
        :key="item.key"
        class="nav-item"
        :class="{ active: route.path === item.path }"
        @click="handleClick(item.path)"
      >
        {{ item.label }}
      </div>
    </nav>
    <div class="right">
      <SemanticSearchBox/>


        <div class="profile">

      <!-- 未登录 -->
      <template v-if="!isLogin">
        <router-link to="/register">
          <BaseButton mode="light" size="medium"> Register </BaseButton>
        </router-link>

        <router-link to="/login">
          <BaseButton mode="light" size="medium"> Login </BaseButton>
        </router-link>
      </template>

      <!-- 已登录 -->
      <template v-else>
        <router-link to="/profile" class="user-box">
          <div class="avatar">
            <!-- 简单头像（SVG） -->
            <svg viewBox="0 0 24 24" fill="none">
              <circle cx="12" cy="8" r="4" stroke="black" stroke-width="2" />
              <path
                d="M4 20c1.5-4 6-6 8-6s6.5 2 8 6"
                stroke="black"
                stroke-width="2"
              />
            </svg>
          </div>

          <span class="username">
            {{ userName }}
          </span>
        </router-link>
      </template>
    </div>
  </div>

  </header>
</template>

<style scoped>
.header {
  position: relative;
  height: 72px;
  padding: 0 48px;
  display: flex;
  align-items: center;
  background: #ffffff;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Arial, sans-serif;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  box-sizing: border-box;
  z-index: 100;
}

.logo {
  font-size: 34px;
  font-weight: 600;
  color: #000;
  letter-spacing: 0;
  white-space: nowrap;
  cursor: pointer;
  z-index: 2;
}

.nav {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: 36px;
  white-space: nowrap;
}

.nav-item {
  font-size: 20px;
  font-weight: 300;
  color: #000;
  cursor: pointer;
  position: relative;
  transition: opacity 0.2s ease;
  letter-spacing: 0.5px;
}

.nav-item:hover {
  opacity: 0.6;
}

.nav-item.active::after {
  content: "";
  position: absolute;
  left: 0;
  bottom: -8px;
  width: 100%;
  height: 2px;
  background: #000;
}

.right {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 16px;
  z-index: 2;
}

.profile {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-box {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  text-decoration: none;
  color: black;
  max-width: 160px;
}

.user-box:hover {
  opacity: 0.7;
}

.avatar {
  width: 32px;
  height: 32px;
  border: 1.5px solid black;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.avatar svg {
  width: 18px;
  height: 18px;
}

.username {
  font-size: 18px;
  max-width: 100px;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
