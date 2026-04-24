<script setup lang="ts">
import { onMounted, computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useApi } from '@/utils/useApi'
import BaseButton from '@/components/BaseButton.vue'
import ProfileEditForm from '@/components/ProfileEditForm.vue'

const router = useRouter()
const { fetchData, data, loading, error } = useApi()

const editing = ref(false)

onMounted(() => {
  fetchData('/api/auth/validate?token=' + localStorage.getItem('token'))
})

const isAdmin = computed(() => data.value?.role === 'ROLE_ADMIN')

const avatarText = computed(() => {
  return data.value?.userName?.charAt(0)?.toUpperCase() || 'U'
})

const roleText = computed(() => {
  if (data.value?.role === 'ROLE_ADMIN') return 'Administrator'
  if (data.value?.role === 'ROLE_USER') return 'Regular User'
  return data.value?.role || 'User'
})

const goConsole = () => {
  router.push('/admin')
}

const logout = () => {
  localStorage.removeItem('accessToken')
  router.push('/login')
}
</script>

<template>
  <div class="profile-page">
    <div class="profile-layout">
      <section class="profile-header">
        <div class="header-left">
          <div class="avatar">
            {{ avatarText }}
          </div>

          <div>
            <h1>{{ data?.userName || 'Profile' }}</h1>
            <p>Manage your personal information and account status.</p>

            <div v-if="data" class="meta-line">
              <span class="role-pill">{{ roleText }}</span>
              <span class="dot"></span>
              <span class="status-text" :class="{ disabled: !data.enable }">
                {{ data.enable ? 'Active account' : 'Disabled account' }}
              </span>
            </div>
          </div>
        </div>

        <div class="header-actions">
          <BaseButton
            v-if="isAdmin"
            mode="dark"
            size="medium"
            @click="goConsole"
          >
            Admin Console
          </BaseButton>

          <BaseButton
            mode="light"
            size="medium"
            @click="editing = !editing"
          >
            {{ editing ? 'Cancel' : 'Edit Profile' }}
          </BaseButton>
        </div>
      </section>

      <div v-if="loading" class="message">Loading profile...</div>
      <div v-else-if="error" class="message error">{{ error }}</div>

      <template v-else-if="data">
        <ProfileEditForm
          v-if="editing"
          :user="data"
          @cancel="editing = false"
        />

        <template v-else>
          <section class="profile-section">
            <div class="section-title">
              <h2>Account</h2>
              <p>Basic identity information for this account.</p>
            </div>

            <div class="info-list">
              <div class="info-row">
                <span class="label">Username</span>
                <span class="value">{{ data.userName || 'Not set' }}</span>
              </div>

              <div class="info-row">
                <span class="label">Role</span>
                <span class="value">{{ roleText }}</span>
              </div>

              <div class="info-row">
                <span class="label">Status</span>
                <span class="value status-value">
                  <i :class="{ disabled: !data.enable }"></i>
                  {{ data.enable ? 'Active' : 'Disabled' }}
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
                <span class="value">{{ data.email || 'Not set' }}</span>
              </div>

              <div class="info-row">
                <span class="label">Phone</span>
                <span class="value">{{ data.phone || 'Not set' }}</span>
              </div>
            </div>
          </section>

          <section class="profile-section danger-section">
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

<style scoped>
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

/* ===== Header ===== */
.profile-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 32px;
  padding-bottom: 28px;
  border-bottom: 1px solid #eee; /* ✅ 保留唯一一条主结构线 */
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.avatar {
  width: 68px;
  height: 68px;
  border-radius: 50%;
  border: 1px solid #ddd;   /* 更轻 */
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-weight: 600;
  color: #111;
  background: white;
}

.profile-header h1 {
  margin: 0;
  font-size: 38px;
  font-weight: 600;
  color: #111;
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
  font-size: 16px;
  background: #f2f2f2; /* ❗去边框，改背景 */
  color: #333;
}

.dot {
  width: 4px;
  height: 4px;
  background: #bbb;
  border-radius: 50%;
}

.status-text {
  font-size: 17px;
  color: #444;
}

.status-text.disabled {
  color: #aaa;
}

.header-actions {
  display: flex;
  gap: 10px;
}

/* ===== Section ===== */
.profile-section {
  display: grid;
  grid-template-columns: 220px 1fr;
  gap: 48px;
  padding: 36px 0;   /* ❗用留白替代分割线 */
}

.section-title h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.section-title p {
  margin-top: 6px;
  font-size: 17px;
  color: #888;
}

/* ===== Info ===== */
.info-list {
  display: flex;
  flex-direction: column;
  gap: 6px;  /* ❗用间距替代边框 */
}

.info-row {
  min-height: 52px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 6px;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.info-row:hover {
  background: #f5f5f5;
}

.label {
  font-size: 17px;
  color: #888;
}

.value {
  font-size: 18px;
  font-weight: 500;
  color: #111;
  max-width: 360px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 状态小点 */
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

/* ===== 消息 ===== */
.message {
  padding: 40px 0;
  color: #777;
}

.error {
  color: #d93025;
}

/* ===== 响应式 ===== */
@media (max-width: 720px) {
  .profile-page {
    padding: 40px 20px;
  }

  .profile-header {
    flex-direction: column;
  }

  .profile-section {
    grid-template-columns: 1fr;
    gap: 20px;
  }
}
</style>