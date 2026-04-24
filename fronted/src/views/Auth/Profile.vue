<script setup lang="ts">
import { onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useApi } from '@/utils/useApi'
import BaseButton from '@/components/BaseButton.vue'
import SETSSLogo from '@/components/SETSSLogo.vue'

const router = useRouter()
const { fetchData, data, loading, error } = useApi()

onMounted(() => {
  fetchData('/api/auth/validate?token=' + localStorage.getItem('token'))
})

const isAdmin = computed(() => {
  return data.value?.role === 'ROLE_ADMIN'
})

const goConsole = () => {
  router.push('/admin')
}
</script>

<template>
  <div class="profile-page">
    <div class="profile-container">
      <SETSSLogo size="medium" subtitle="User Center" />

      <div class="profile-card">
        <div class="card-header">
          <div class="avatar">
            {{ data?.userName?.charAt(0)?.toUpperCase() || 'U' }}
          </div>

          <div class="user-title">
            <h1>{{ data?.userName || 'Profile' }}</h1>
            <p>Personal account information</p>
          </div>
        </div>

        <div v-if="loading" class="loading">
          Loading...
        </div>

        <div v-else-if="error" class="error">
          {{ error }}
        </div>

        <div v-else-if="data" class="info-list">
          <div class="info-item">
            <span class="label">Username</span>
            <span class="value">{{ data.userName || 'Not set' }}</span>
          </div>

          <div class="info-item">
            <span class="label">Email</span>
            <span class="value">{{ data.email || 'Not set' }}</span>
          </div>

          <div class="info-item">
            <span class="label">Phone</span>
            <span class="value">{{ data.phone || 'Not set' }}</span>
          </div>

          <div class="info-item">
            <span class="label">Status</span>
            <span class="status" :class="{ disabled: !data.enable }">
              {{ data.enable ? 'Active' : 'Disabled' }}
            </span>
          </div>

          <div class="info-item">
            <span class="label">Role</span>
            <span class="value">{{ data.role || 'User' }}</span>
          </div>
        </div>

        <div class="actions">
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
            @click="router.push('/')"
          >
            Back Home
          </BaseButton>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.profile-page {
  min-height: 100vh;
  width: 100%;
  background: #f7f7f7;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px 24px;
  box-sizing: border-box;
}

.profile-container {
  width: 100%;
  max-width: 520px;
}

.profile-card {
  width: 100%;
  background: white;
  border: 1.5px solid black;
  border-radius: 22px;
  padding: 36px 34px;
  box-sizing: border-box;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 18px;
  margin-bottom: 34px;
}

.avatar {
  width: 64px;
  height: 64px;
  border: 1.5px solid black;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 28px;
  font-weight: 700;
  color: black;
  background: white;
}

.user-title h1 {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  color: black;

  max-width: 320px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-title p {
  margin: 6px 0 0;
  font-size: 14px;
  color: #666;
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.info-item {
  height: 52px;
  border: 1.5px solid black;
  border-radius: 14px;
  padding: 0 18px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  box-sizing: border-box;
}

.label {
  font-size: 14px;
  color: #666;
}

.value {
  font-size: 15px;
  font-weight: 500;
  color: black;
  max-width: 260px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.status {
  font-size: 13px;
  font-weight: 600;
  padding: 5px 12px;
  border: 1.5px solid black;
  border-radius: 999px;
  background: black;
  color: white;
}

.status.disabled {
  background: white;
  color: black;
}

.actions {
  margin-top: 28px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.loading,
.error {
  text-align: center;
  font-size: 14px;
  padding: 32px 0;
}

.error {
  color: #d93025;
}
</style>