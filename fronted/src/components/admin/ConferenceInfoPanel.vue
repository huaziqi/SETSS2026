<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useApi } from '@/utils/useApi'

interface ConferencePage {
  pageId: number
  pageKey: string
  title: string
  content: string
  status: string
  publishTime?: string
  updateTime?: string
}

const emit = defineEmits<{
  (e: 'notify', payload: { type: 'success' | 'error'; text: string }): void
}>()

const router = useRouter()
const { data, fetchData } = useApi()

const pages = ref<ConferencePage[]>([])

const loadPages = async () => {
  try {
    await fetchData('/api/admin/pages')
    pages.value = data.value?.data || data.value || []
  } catch {
    emit('notify', {
      type: 'error',
      text: 'Failed to load conference pages.'
    })
  }
}

const goEdit = (pageKey: string) => {
  router.push(`/admin/conference/edit/${pageKey}`)
}

onMounted(loadPages)

defineExpose({ loadPages })
</script>

<template>
  <section class="panel">
    <div class="panel-header">

      <button @click="loadPages">Refresh</button>
    </div>

    <table class="data-table">
      <thead>
        <tr>
          <th>Page Key</th>
          <th>Title</th>
          <th>Status</th>
          <th>Updated</th>
          <th>Actions</th>
        </tr>
      </thead>

      <tbody>
        <tr v-for="page in pages" :key="page.pageKey">
          <td>{{ page.pageKey }}</td>
          <td>{{ page.title }}</td>
          <td>{{ page.status }}</td>
          <td>{{ page.updateTime?.replace('T', ' ') || '-' }}</td>
          <td>
            <button @click="goEdit(page.pageKey)">Edit</button>
          </td>
        </tr>

        <tr v-if="pages.length === 0">
          <td colspan="5" class="empty">
            No conference pages found.
          </td>
        </tr>
      </tbody>
    </table>
  </section>
</template>

<style scoped>
.panel {
  background: #fff;
  border: 1px solid #e5e5e5;
  padding: 24px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 18px;
}

.panel h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
}

.panel p {
  margin: 6px 0 0;
  color: #777;
  font-size: 14px;
}

button {
  border: 1px solid #111;
  background: #111;
  color: #fff;
  padding: 8px 14px;
  cursor: pointer;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th,
.data-table td {
  border: 1px solid #ececec;
  padding: 9px;
  text-align: left;
  font-size: 14px;
}

.data-table th {
  background: #fafafa;
  font-weight: 600;
}

.empty {
  text-align: center;
  color: #777;
  padding: 24px;
}
</style>