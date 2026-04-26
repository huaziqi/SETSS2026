<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useApi } from '@/utils/useApi'
import AdminDashboard from '@/components/admin/AdminDashboard.vue'
import PostManagementPanel from '@/components/admin/PostManagementPanel.vue'
import CommentManagementPanel from '@/components/admin/CommentManagementPanel.vue'
import UserManagementPanel from '@/components/admin/UserManagementPanel.vue'
import ConferenceInfoPanel from '@/components/admin/ConferenceInfoPanel.vue'

interface PostItem {
  postId: number
  title: string
  status: string
  viewCount: number
  isPinned: boolean
}

const router = useRouter()
const { validate, data, fetchData } = useApi()

const userName = ref('')
const role = ref('')
const activeMenu = ref('dashboard')
const allowedRoles = ['ROLE_ADMIN', 'ROLE_CHAIR', 'ROLE_VIEWER']


const menuList = [
  { key: 'dashboard', label: 'Dashboard' },
  { key: 'users', label: 'User Management' },
  { key: 'posts', label: 'Post Management' },
  { key: 'comments', label: 'Comment Management' },
  { key: 'conference', label: 'Conference Info' }
]

const posts = ref<PostItem[]>([])
const message = ref('')
const errorMsg = ref('')

const goHome = () => {
  router.push('/')
}

const handleMenuClick = (key: string) => {
  activeMenu.value = key
}

const handlePostsUpdated = (list: PostItem[]) => {
  posts.value = list
}

const handleNotify = (payload: { type: 'success' | 'error'; text: string }) => {
  if (payload.type === 'success') {
    message.value = payload.text
    errorMsg.value = ''
  } else {
    errorMsg.value = payload.text
    message.value = ''
  }
}

const loadPostsForDashboard = async () => {
  await fetchData('/api/admin/posts')
  posts.value = data.value?.data || data.value || []
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
    return
  }

  await loadPostsForDashboard()
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
          <button class="home-btn" @click="goHome">
            Home
          </button>
        </div>
      </header>

      <main class="content">
        <p v-if="message" class="msg success">{{ message }}</p>
        <p v-if="errorMsg" class="msg error">{{ errorMsg }}</p>

        <AdminDashboard
          v-if="activeMenu === 'dashboard'"
          :posts="posts"
        />

        <UserManagementPanel
          v-else-if="activeMenu === 'users'"
          @notify="handleNotify"
        />

        <PostManagementPanel
          v-else-if="activeMenu === 'posts'"
          @posts-updated="handlePostsUpdated"
          @notify="handleNotify"
        />

        <CommentManagementPanel
          v-else-if="activeMenu === 'comments'"
          :posts="posts"
          @notify="handleNotify"
        />

        <ConferenceInfoPanel
          v-else-if="activeMenu === 'conference'"
          @notify="handleNotify"
        />

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

.panel h2 {
  margin: 0 0 14px;
}

.home-btn {
  height: 36px;
  padding: 0 14px;
  border: 1px solid #111;
  background: #fff;
  color: #111;
  cursor: pointer;
}

.home-btn:hover {
  background: #111;
  color: #fff;
}
</style>