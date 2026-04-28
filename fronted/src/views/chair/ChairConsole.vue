<script setup lang="ts">
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { useApi } from "@/utils/useApi";
import ChairUserManagementPanel from "@/components/chair/ChairUserManagementPanel.vue";
import ManuscriptAssignPanel from "@/components/chair/ManuscriptAssignPanel.vue";
import ManuDetail from "@/components/chair/ManuDetail.vue";

const router = useRouter();
const { validate, data } = useApi();

const userName = ref("");
const role = ref("");
// 🔥 修改：默认选中第一个有效菜单 'users'
const activeMenu = ref("users");
const viewingManuId = ref<number | null>(null);

// 🔥 修改：精简菜单列表，只保留用户管理和稿件分配
const menuList = [
  { key: "users", label: "User Management" },
  { key: "assign", label: "Assign Manuscripts" },
];

const message = ref("");
const errorMsg = ref("");

const handleNotify = (payload: { type: "success" | "error"; text: string }) => {
  if (payload.type === "success") {
    message.value = payload.text;
    errorMsg.value = "";
  } else {
    errorMsg.value = payload.text;
    message.value = "";
  }
};

const handleMenuClick = (key: string) => {
  activeMenu.value = key;
  viewingManuId.value = null;
};

const handleViewDetail = (id: number) => {
  viewingManuId.value = id;
};

const handleCloseDetail = () => {
  viewingManuId.value = null;
};

const goHome = () => router.push("/");
const logout = () => {
  localStorage.removeItem("accessToken");
  localStorage.removeItem("id");
  router.push("/login");
};

onMounted(async () => {
  const isValid = await validate();
  if (!isValid) {
    router.push("/login");
    return;
  }
  userName.value = data.value.userName || "";
  role.value = data.value.role || "";

  if (role.value !== "ROLE_CHAIR") {
    alert("Access Denied");
    router.push("/");
  }
});
</script>

<template>
  <div class="admin-page">
    <aside class="sidebar">
      <div class="brand">
        <h2>SETSS 2026</h2>
        <p>Chair Console</p>
      </div>
      <nav class="menu">
        <button
          v-for="item in menuList"
          :key="item.key"
          class="menu-item"
          :class="{ active: activeMenu === item.key }"
          @click="handleMenuClick(item.key)"
        >
          {{ item.label }}
        </button>
      </nav>
    </aside>

    <div class="admin-main">
      <header class="admin-header">
        <div>
          <h1 v-if="viewingManuId">Manuscript Detail</h1>
          <h1 v-else>
            {{ menuList.find((i) => i.key === activeMenu)?.label }}
          </h1>
          <p v-if="!viewingManuId">Welcome, Chair {{ userName }}</p>
        </div>
        <div class="user-area">
          <button class="logout-btn" @click="logout">Logout</button>
          <button class="home-btn" @click="goHome">Home</button>
        </div>
      </header>

      <main class="content">
        <p v-if="message" class="msg success">{{ message }}</p>
        <p v-if="errorMsg" class="msg error">{{ errorMsg }}</p>

        <!-- 🔥 修改：移除 overview 和 settings 相关的模板代码 -->

        <!-- 用户管理 -->
        <ChairUserManagementPanel
          v-if="activeMenu === 'users'"
          @notify="handleNotify"
        />

        <!-- 稿件分配 -->
        <template v-if="activeMenu === 'assign'">
          <ManuDetail
            v-if="viewingManuId"
            :manu-id="viewingManuId"
            @back="handleCloseDetail"
            @notify="handleNotify"
          />
          <ManuscriptAssignPanel
            v-else
            @notify="handleNotify"
            @view-detail="handleViewDetail"
          />
        </template>
      </main>
    </div>
  </div>
</template>

<style scoped>
/* 样式保持不变 */
.admin-page {
  min-height: 100vh;
  display: flex;
  background: #f6f6f6;
  color: #111;
}
.sidebar {
  width: 260px;
  background: #111;
  color: #fff;
  padding: 24px 18px;
  box-sizing: border-box;
}
.brand {
  margin-bottom: 36px;
}
.brand h2 {
  margin: 0;
  font-size: 24px;
}
.brand p {
  margin: 6px 0 0;
  font-size: 13px;
  color: #aaa;
}
.menu {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.menu-item {
  height: 42px;
  border: none;
  background: transparent;
  color: #ccc;
  text-align: left;
  padding: 0 12px;
  font-size: 15px;
  cursor: pointer;
}
.menu-item:hover,
.menu-item.active {
  background: #fff;
  color: #111;
}
.admin-main {
  flex: 1;
  min-width: 0;
}
.admin-header {
  min-height: 86px;
  background: #fff;
  border-bottom: 1px solid #e5e5e5;
  padding: 12px 32px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.admin-header h1 {
  margin: 0;
  font-size: 24px;
}
.user-area {
  display: flex;
  gap: 16px;
}
.logout-btn,
.home-btn {
  height: 36px;
  padding: 0 14px;
  border: 1px solid #111;
  cursor: pointer;
}
.logout-btn {
  background: #111;
  color: #fff;
}
.home-btn {
  background: #fff;
  color: #111;
}
.content {
  padding: 28px 32px;
}
.msg {
  margin: 0 0 12px;
  padding: 10px 12px;
  border-radius: 6px;
}
.success {
  background: #eafff1;
  color: #155724;
}
.error {
  background: #fff0f0;
  color: #a11313;
}
.panel {
  background: #fff;
  border: 1px solid #e5e5e5;
  padding: 24px;
}
</style>
