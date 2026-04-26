<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useApi } from '@/utils/useApi'

const router = useRouter()
const { validate, data } = useApi()

const userName = ref('')
const role = ref('')
const activeMenu = ref('dashboard')

const allowedRoles = ['ROLE_ADMIN', 'ROLE_CHAIR', 'ROLE_VIEWER']

const menuList = [
  { key: 'dashboard', label: 'Dashboard' },
  { key: 'users', label: 'User Management' },
  { key: 'posts', label: 'Post Management' },
  { key: 'conference', label: 'Conference Info' },
  { key: 'settings', label: 'Settings' }
]

const handleMenuClick = (key: string) => {
  activeMenu.value = key
}

const logout = () => {
  localStorage.removeItem('accessToken')
  router.push('/login')
}

onMounted(async () => {
  const isValid = await validate()

  if (!isValid) {
    router.push('/login')
    return
  }

  userName.value = data.value.userName || data.value.username || ''
  role.value = data.value.role || ''

  if (!allowedRoles.includes(role.value)) {
    alert('You do not have permission to access this page.')
    router.push('/')
  }
})
</script>

<template>
  <div class="admin-page">
    <aside class="sidebar">
      <div class="brand">
        <h2>SETSS 2026</h2>
        <p>Admin Console</p>
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
          <h1>{{ menuList.find(item => item.key === activeMenu)?.label }}</h1>
          <p>Manage SETSS 2026 platform data and content.</p>
        </div>

        <div class="user-area">
          <div class="user-info">
            <strong>{{ userName }}</strong>
            <span>{{ role }}</span>
          </div>

          <button class="logout-btn" @click="logout">
            Logout
          </button>
        </div>
      </header>

      <main class="content">
        <section v-if="activeMenu === 'dashboard'" class="panel">
          <h2>Dashboard</h2>
          <p>Overview statistics will be displayed here.</p>
        </section>

        <section v-else-if="activeMenu === 'users'" class="panel">
          <h2>User Management</h2>
          <p>Manage registered users and user roles.</p>
        </section>

        <section v-else-if="activeMenu === 'posts'" class="panel">
          <h2>Post Management</h2>
          <p>Review, hide, delete, or pin forum posts.</p>
        </section>

        <section v-else-if="activeMenu === 'conference'" class="panel">
          <h2>Conference Info</h2>
          <p>Edit schedule, speakers, registration, and conference pages.</p>
        </section>

        <section v-else-if="activeMenu === 'settings'" class="panel">
          <h2>Settings</h2>
          <p>System settings and platform configuration.</p>
        </section>
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
  height: 86px;
  background: #fff;
  border-bottom: 1px solid #e5e5e5;
  padding: 0 32px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.admin-header h1 {
  margin: 0;
  font-size: 24px;
}

.admin-header p {
  margin: 6px 0 0;
  color: #777;
  font-size: 14px;
}

.user-area {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-info {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.user-info strong {
  font-size: 15px;
}

.user-info span {
  margin-top: 4px;
  font-size: 12px;
  color: #777;
}

.logout-btn {
  height: 36px;
  padding: 0 14px;
  border: 1px solid #111;
  background: #111;
  color: #fff;
  cursor: pointer;
}

.content {
  padding: 28px 32px;
}

.panel {
  background: #fff;
  border: 1px solid #e5e5e5;
  padding: 24px;
}

.panel h2 {
  margin: 0 0 10px;
}
</style>