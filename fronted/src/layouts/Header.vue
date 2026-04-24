<script setup lang="ts">
import BaseButton from "../components/BaseButton.vue"
import { ref, onMounted } from "vue"
import { useApi } from "../utils/useApi"



// 引入 useRouter 用于编程式导航（如果需要），但这里我们主要用 router-link
import { useRouter } from "vue-router";

const router = useRouter();

const isLogin = ref(false)
const active = ref("home")
const userName = ref("")
const { fetchData, data } = useApi()

onMounted(async () => {
  const token = localStorage.getItem('accessToken')
  if (token) {
    await fetchData('/api/auth/validate?token=' + token)

    console.log(data.value)
    if (data.value && data.value.error === null) {
      isLogin.value = true;
      userName.value = data.value.userName;
    }
  }
})


const menu = [
  { key: "home", label: "Home", path: "/" },
  { key: "schedule", label: "Schedule", path: "/schedule" }, // 假设路径，可根据实际调整
  { key: "courses", label: "Courses", path: "/courses" }, // 假设路径
  { key: "manuscript", label: "Manuscript", path: "/submit" }, // 新增稿件提交
  { key: "about", label: "About", path: "/about" }, // 假设路径
];

const handleClick = (key: string, path: string) => {
  active.value = key;
  if (key === "manuscript") {
    router.push(path);
  }
};
</script>

<template>
  <header class="header">
    <!-- Logo 部分，点击可回家 -->
    <div class="logo" @click="handleClick('home', '/')">SETSS 2026</div>

    <nav class="nav">
      <div
        v-for="item in menu"
        :key="item.key"
        class="nav-item"
        :class="{ active: active === item.key }"
        @click="handleClick(item.key, item.path)"
      >
        {{ item.label }}
      </div>
    </nav>
  
    <div class="profile">

      <!-- 未登录 -->
      <template v-if="!isLogin">
        <router-link to="/register">
          <BaseButton mode="light" size="medium">
            Register
          </BaseButton>
        </router-link>

        <router-link to="/login">
          <BaseButton mode="light" size="medium">
            Login
          </BaseButton>
        </router-link>
      </template>

      <!-- 已登录 -->
      <template v-else>
    <router-link to="/profile" class="user-box">
    
    <div class="avatar">
      <!-- 简单头像（SVG） -->
      <svg viewBox="0 0 24 24" fill="none">
        <circle cx="12" cy="8" r="4" stroke="black" stroke-width="2"/>
        <path d="M4 20c1.5-4 6-6 8-6s6.5 2 8 6" stroke="black" stroke-width="2"/>
      </svg>
    </div>

    <span class="username">
      {{ userName }}
    </span>

  </router-link>
</template>

    </div>
  </header>
</template>

<style scoped>
.header {
  height: 72px; /* 稍微加高一点更高级 */
  padding: 0 48px;
  display: flex;
  justify-content: space-between;
  align-items: center;

  background: #ffffff;

  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Arial, sans-serif;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05); /* 加一点阴影增加层次感 */
}

/* Logo */
.logo {
  font-size: 34px;
  font-weight: 600;
  color: #000;
  letter-spacing: 0px;
}

.nav {
  display: flex;
  gap: 36px;
}

.nav-item {
  font-size: 20px;
  font-weight: 300;
  color: #000;
  cursor: pointer;
  position: relative;
  transition: all 0.2s ease;

  letter-spacing: 0.5px;
}

/* hover */
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
.profile {
  display: flex;
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

/* hover效果 */
.user-box:hover {
  opacity: 0.7;
}

/* 头像 */
.avatar {
  width: 32px;
  height: 32px;
  border: 1.5px solid black;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar svg {
  width: 18px;
  height: 18px;
}

/* 用户名（重点） */
.username {
  font-size: 18px;
  max-width: 100px;
  font-weight: 500;

  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

</style>
