<script setup lang="ts">
import { ref } from "vue";
import BaseButton from "../components/BaseButton.vue";

// 引入 useRouter 用于编程式导航（如果需要），但这里我们主要用 router-link
import { useRouter } from "vue-router";

const router = useRouter();

const active = ref("home");

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
      <router-link to="/register">
        <BaseButton mode="light" size="medium"> Register </BaseButton>
      </router-link>

      <router-link to="/login">
        <BaseButton mode="light" size="medium"> Login </BaseButton>
      </router-link>
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
</style>
