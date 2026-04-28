<script setup lang="ts">
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { useApi } from "@/utils/useApi";
import ReviewPanel from "@/components/reviewer/ReviewPanel.vue";
import ManuscriptViewDetail from "@/components/reviewer/ManuscriptViewDetail.vue";
import ReviewedManuscripts from "@/components/reviewer/ReviewedManuscripts.vue";
import ReviewedManudetail from "@/components/reviewer/ReviewedManudetail.vue";

const router = useRouter();
const { validate, data } = useApi();

const userName = ref("");
const role = ref("");
const activeMenu = ref("review");

const menuList = [
  { key: "review", label: "Review Manuscripts" },
  { key: "reviewed", label: "Reviewed History" },
];

const message = ref("");
const errorMsg = ref("");

const reviewingManuId = ref<number | null>(null);
const viewingReviewedManuId = ref<number | null>(null);

const handleNotify = (payload: { type: "success" | "error"; text: string }) => {
  if (payload.type === "success") {
    message.value = payload.text;
    errorMsg.value = "";
  } else {
    errorMsg.value = payload.text;
    message.value = "";
  }
};

const handleReview = (id: number) => {
  reviewingManuId.value = id;
};

const handleCloseDetail = () => {
  reviewingManuId.value = null;
};

const handleViewReviewed = (id: number) => {
  viewingReviewedManuId.value = id;
};

const handleCloseReviewedDetail = () => {
  viewingReviewedManuId.value = null;
};

const handleMenuClick = (key: string) => {
  activeMenu.value = key;
  reviewingManuId.value = null;
  viewingReviewedManuId.value = null;
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

  if (role.value !== "ROLE_REVIEWER") {
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
        <p>Reviewer Console</p>
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
          <h1 v-if="reviewingManuId">Manuscript Review Detail</h1>
          <h1 v-else-if="viewingReviewedManuId">Reviewed Manuscript Detail</h1>
          <h1 v-else>
            {{ menuList.find((item) => item.key === activeMenu)?.label }}
          </h1>
          <p>Welcome, Reviewer {{ userName }}</p>
        </div>
        <div class="user-area">
          <button class="logout-btn" @click="logout">Logout</button>
          <button class="home-btn" @click="goHome">Home</button>
        </div>
      </header>

      <main class="content">
        <p v-if="message" class="msg success">{{ message }}</p>
        <p v-if="errorMsg" class="msg error">{{ errorMsg }}</p>

        <template v-if="activeMenu === 'review'">
          <ManuscriptViewDetail
            v-if="reviewingManuId"
            :manu-id="reviewingManuId"
            @back="handleCloseDetail"
            @notify="handleNotify"
          />

          <ReviewPanel
            v-else
            @notify="handleNotify"
            @review="handleReview"
          />
        </template>

        <template v-else-if="activeMenu === 'reviewed'">
          <ReviewedManudetail
            v-if="viewingReviewedManuId"
            :manu-id="viewingReviewedManuId"
            @back="handleCloseReviewedDetail"
            @notify="handleNotify"
          />

          <ReviewedManuscripts
            v-else
            @notify="handleNotify"
            @view-detail="handleViewReviewed"
          />
        </template>
      </main>
    </div>
  </div>
</template>

<style scoped>
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
</style>





