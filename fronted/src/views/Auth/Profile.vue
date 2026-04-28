<script setup lang="ts">
import { onMounted, computed, ref } from "vue";
import { useRouter } from "vue-router";
import { useApi } from "@/utils/useApi";
import BaseButton from "@/components/BaseButton.vue";
import ProfileEditForm from "@/components/ProfileEditForm.vue";

const router = useRouter();
const { fetchData, data, loading, error } = useApi();

const editing = ref(false);

onMounted(() => {
  fetchData("/api/auth/validate?token=" + localStorage.getItem("accessToken"));
});

// 🔥 定义角色判断
const isAdmin = computed(() => data.value?.role === "ROLE_ADMIN");
const isChair = computed(() => data.value?.role === "ROLE_CHAIR");
const isReviewer = computed(() => data.value?.role === "ROLE_REVIEWER");

const avatarText = computed(() => {
  return data.value?.userName?.charAt(0)?.toUpperCase() || "U";
});

const roleText = computed(() => {
  if (data.value?.role === "ROLE_ADMIN") return "Administrator";
  if (data.value?.role === "ROLE_CHAIR") return "Conference Chair";
  if (data.value?.role === "ROLE_REVIEWER") return "Reviewer";
  if (data.value?.role === "ROLE_USER") return "Regular User";
  return data.value?.role || "User";
});

// 🔥 动态跳转逻辑
const goConsole = () => {
  if (isAdmin.value) {
    router.push("/admin");
  } else if (isChair.value) {
    router.push("/chair");
  } else if (isReviewer.value) {
    router.push("/reviewer");
  }
};

const goBack = () => {
  router.push("/");
};

const logout = () => {
  localStorage.removeItem("accessToken");
  localStorage.removeItem("id");
  router.push("/login");
};
</script>

<template>
  <div class="profile-page">
    <div class="profile-layout">
      <section class="profile-header">
        <div class="header-left">
          <button class="back-btn" @click="goBack" title="Back Home">←</button>
          <div class="avatar">{{ avatarText }}</div>
          <div class="header-info">
            <h1>{{ data?.userName || "Profile" }}</h1>
            <p>Manage your personal information and account status.</p>
            <div v-if="data" class="meta-line">
              <span class="role-pill">{{ roleText }}</span>
              <span class="dot"></span>
              <span class="status-text" :class="{ disabled: !data.enable }">
                {{ data.enable ? "Active account" : "Disabled account" }}
              </span>
            </div>
          </div>
        </div>

        <div class="header-actions">
          <!-- 🔥 管理员入口 -->
          <BaseButton
            v-if="isAdmin"
            mode="dark"
            size="medium"
            @click="goConsole"
          >
            Admin Console
          </BaseButton>

          <!-- 🔥 Chair 入口 -->
          <BaseButton
            v-if="isChair"
            mode="dark"
            size="medium"
            @click="goConsole"
          >
            Chair Console
          </BaseButton>

          <!-- 🔥 Reviewer 入口 -->
          <BaseButton
            v-if="isReviewer"
            mode="dark"
            size="medium"
            @click="goConsole"
          >
            Reviewer Console
          </BaseButton>

          <BaseButton mode="dark" size="medium" @click="editing = !editing">
            {{ editing ? "Cancel" : "Edit Profile" }}
          </BaseButton>
        </div>
      </section>

      <!-- 其余部分保持不变 -->
      <div v-if="loading" class="message">Loading profile...</div>
      <div v-else-if="error" class="message error">{{ error }}</div>

      <template v-else-if="data">
        <ProfileEditForm
          v-if="editing"
          :user="data"
          @cancel="editing = false"
        />
        <template v-else>
          <!-- ... existing profile sections ... -->
          <section class="profile-section">
            <div class="section-title">
              <h2>Account</h2>
              <p>Basic identity information for this account.</p>
            </div>
            <div class="info-list">
              <div class="info-row">
                <span class="label">Username</span>
                <span class="value">{{ data.userName || "Not set" }}</span>
              </div>
              <div class="info-row">
                <span class="label">Role</span>
                <span class="value">{{ roleText }}</span>
              </div>
              <div class="info-row">
                <span class="label">Status</span>
                <span class="value status-value">
                  <i :class="{ disabled: !data.enable }"></i>
                  {{ data.enable ? "Active" : "Disabled" }}
                </span>
              </div>
            </div>
          </section>
          <section class="profile-section">
            <div class="section-title">
              <h2>Contact</h2>
              <p>Information used for account contact and verification.</p>
            </div>
            <div class="info-list">
              <div class="info-row">
                <span class="label">Email</span>
                <span class="value">{{ data.email || "Not set" }}</span>
              </div>
              <div class="info-row">
                <span class="label">Phone</span>
                <span class="value">{{ data.phone || "Not set" }}</span>
              </div>
            </div>
          </section>
          <section class="profile-section">
            <div class="section-title">
              <h2>Session</h2>
              <p>You can sign out of the current account here.</p>
            </div>
            <div class="info-list">
              <div class="info-row">
                <span class="label">Current session</span>
                <BaseButton mode="light" size="small" @click="logout">
                  Logout
                </BaseButton>
              </div>
            </div>
          </section>
        </template>
      </template>
    </div>
  </div>
</template>

<!-- Style 保持不变 -->
<style scoped>
/* ... existing styles ... */
.profile-page {
  min-height: 100vh;
  background: #fafafa;
  padding: 64px 80px;
  box-sizing: border-box;
}
.profile-layout {
  max-width: 900px;
  margin: 0 auto;
}
.profile-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 32px;
  padding-bottom: 28px;
  border-bottom: 1px solid #eee;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 18px;
  min-width: 0;
}
.back-btn {
  width: 34px;
  height: 34px;
  border: none;
  background: transparent;
  color: #666;
  font-size: 26px;
  line-height: 1;
  cursor: pointer;
  border-radius: 50%;
  transition: all 0.2s ease;
  flex-shrink: 0;
}
.back-btn:hover {
  background: #f0f0f0;
  color: #111;
  transform: translateX(-2px);
}
.avatar {
  width: 68px;
  height: 68px;
  border-radius: 50%;
  border: 1px solid #ddd;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-weight: 600;
  color: #111;
  background: white;
  flex-shrink: 0;
}
.header-info {
  min-width: 0;
}
.profile-header h1 {
  margin: 0;
  max-width: 420px;
  font-size: 38px;
  font-weight: 600;
  color: #111;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.profile-header p {
  margin: 8px 0 0;
  font-size: 14px;
  color: #777;
}
.meta-line {
  margin-top: 12px;
  display: flex;
  align-items: center;
  gap: 10px;
}
.role-pill {
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  background: #f2f2f2;
  color: #333;
}
.dot {
  width: 4px;
  height: 4px;
  background: #bbb;
  border-radius: 50%;
}
.status-text {
  font-size: 13px;
  color: #444;
}
.status-text.disabled {
  color: #aaa;
}
.header-actions {
  display: flex;
  gap: 10px;
  flex-shrink: 0;
}
.profile-section {
  display: grid;
  grid-template-columns: 220px 1fr;
  gap: 48px;
  padding: 36px 0;
}
.section-title h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}
.section-title p {
  margin-top: 6px;
  font-size: 13px;
  color: #888;
  line-height: 1.6;
}
.info-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.info-row {
  min-height: 52px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 32px;
  padding: 0 6px;
  border-radius: 8px;
  transition: all 0.2s ease;
}
.info-row:hover {
  background: #f5f5f5;
}
.label {
  font-size: 13px;
  color: #888;
}
.value {
  font-size: 14px;
  font-weight: 500;
  color: #111;
  max-width: 360px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.status-value {
  display: flex;
  align-items: center;
  gap: 6px;
}
.status-value i {
  width: 6px;
  height: 6px;
  background: #111;
  border-radius: 50%;
}
.status-value i.disabled {
  background: #bbb;
}
.message {
  padding: 40px 0;
  color: #777;
}
.error {
  color: #d93025;
}
@media (max-width: 720px) {
  .profile-page {
    padding: 40px 20px;
  }
  .profile-header {
    flex-direction: column;
  }
  .header-left {
    align-items: flex-start;
  }
  .profile-header h1 {
    font-size: 34px;
    max-width: 240px;
  }
  .profile-section {
    grid-template-columns: 1fr;
    gap: 20px;
  }
  .header-actions {
    width: 100%;
    flex-wrap: wrap;
  }
  .info-row {
    align-items: flex-start;
    flex-direction: column;
    gap: 8px;
    padding: 12px 6px;
  }
}
</style>
