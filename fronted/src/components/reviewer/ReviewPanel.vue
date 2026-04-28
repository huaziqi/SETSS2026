<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { useApi } from "@/utils/useApi";

interface ManuscriptItem {
  manuscriptId: number;
  title: string;
  author: string;
  introduction?: string;
  originalFileName?: string;
  publishTime: string;
  updateTime: string;
  status: string;
}

const emit = defineEmits<{
  (e: "notify", payload: { type: "success" | "error"; text: string }): void;
  (e: "review", id: number): void;
}>();

const { data, fetchData } = useApi();

const manuscripts = ref<ManuscriptItem[]>([]);
const keyword = ref("");
const loading = ref(false);

// 格式化时间
const formatDate = (dateStr: string) => {
  if (!dateStr) return "-";
  return dateStr.replace("T", " ").substring(0, 19);
};

const filteredManuscripts = computed(() => {
  const key = keyword.value.trim().toLowerCase();
  const validList = manuscripts.value.filter((item) => item != null);

  if (!key) return validList;

  return validList.filter(
    (item) =>
      item.title?.toLowerCase().includes(key) ||
      item.author?.toLowerCase().includes(key) ||
      String(item.manuscriptId).includes(key),
  );
});

const loadManuscripts = async () => {
  loading.value = true;
  try {
    // 从 localStorage 获取审稿人 ID
    const reviewerId = localStorage.getItem("id");
    if (!reviewerId) {
      emit("notify", { type: "error", text: "Reviewer ID not found." });
      manuscripts.value = [];
      return;
    }

    await fetchData(`/api/manuscript/review/get/${reviewerId}`);
    const response = data.value;

    if (response && response.code === 200) {
      const rawList = response.data || [];
      manuscripts.value = rawList
        .filter((item: any) => item != null && typeof item === "object")
        .map((item: any) => ({
          manuscriptId: item.manuscriptId || 0,
          title: item.title || "Untitled",
          author: item.author || "Unknown",
          introduction: item.introduction,
          originalFileName: item.originalFileName,
          publishTime: item.publishTime,
          updateTime: item.updateTime,
          status: item.status || "AwaitingReviewing",
        }));
    } else {
      const errorMsg = response?.message || "Failed to load manuscripts.";
      emit("notify", { type: "error", text: errorMsg });
      manuscripts.value = [];
    }
  } catch (error) {
    console.error(error);
    emit("notify", { type: "error", text: "Network error or server issue." });
    manuscripts.value = [];
  } finally {
    loading.value = false;
  }
};

const handleReview = (item: ManuscriptItem) => {
  emit("review", item.manuscriptId);
};

onMounted(loadManuscripts);

defineExpose({ loadManuscripts });
</script>

<template>
  <section class="panel">
    <div class="panel-header">
      <h2>Review Manuscripts</h2>
      <button @click="loadManuscripts" :disabled="loading">
        {{ loading ? "Loading..." : "Refresh List" }}
      </button>
    </div>

    <div class="toolbar">
      <input v-model="keyword" placeholder="Search by Title or Author" />
    </div>

    <div class="table-wrapper">
      <table class="data-table">
        <thead>
          <tr>
            <th>Title</th>
            <th style="width: 150px">Author</th>
            <th style="width: 150px">Publish Time</th>
            <th style="width: 150px">Update Time</th>
            <th style="width: 120px">Status</th>
            <th style="width: 100px" class="action-col">Actions</th>
          </tr>
        </thead>

        <tbody>
          <tr v-for="item in filteredManuscripts" :key="item.manuscriptId">
            <td>
              <div class="title-cell">
                <strong>{{ item.title }}</strong>
                <span v-if="item.originalFileName" class="file-hint">
                  📎 {{ item.originalFileName }}
                </span>
              </div>
            </td>

            <td>{{ item.author }}</td>
            <td>{{ formatDate(item.publishTime) }}</td>
            <td>{{ formatDate(item.updateTime) }}</td>

            <td>
              <span class="status-badge pending">
                {{ item.status || "AwaitingReviewing" }}
              </span>
            </td>

            <td class="action-col">
              <button class="review-btn" @click="handleReview(item)">
                Review
              </button>
            </td>
          </tr>

          <tr v-if="filteredManuscripts.length === 0 && !loading">
            <td colspan="6" class="empty">No manuscripts awaiting review.</td>
          </tr>

          <tr v-if="loading">
            <td colspan="6" class="loading-row">Loading data...</td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>

<style scoped>
.panel {
  background: #fff;
  border: 1px solid #e5e5e5;
  padding: 24px;
  display: flex;
  flex-direction: column;
  height: 100%;
}
.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
}
.panel-header h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
}
.toolbar {
  margin-bottom: 14px;
}
.toolbar input {
  width: 100%;
  max-width: 400px;
  border: 1px solid #ddd;
  border-radius: 6px;
  padding: 8px 10px;
  font-size: 14px;
}
.table-wrapper {
  overflow-y: auto;
  max-height: 600px;
  border: 1px solid #ececec;
  border-radius: 6px;
}
.data-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 800px;
}
.data-table th,
.data-table td {
  border-bottom: 1px solid #ececec;
  padding: 12px;
  text-align: left;
  vertical-align: middle;
  font-size: 14px;
}
.data-table th {
  background: #fafafa;
  font-weight: 600;
  position: sticky;
  top: 0;
  z-index: 10;
}
.data-table tr:last-child td {
  border-bottom: none;
}
.title-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.file-hint {
  font-size: 12px;
  color: #666;
}
.status-badge {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}
.status-badge.pending {
  background: #fff7ed;
  color: #c2410c;
  border: 1px solid #ffedd5;
}
.action-col {
  text-align: center;
}
.review-btn {
  background: #111;
  color: #fff;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
  transition: opacity 0.2s;
}
.review-btn:hover {
  opacity: 0.8;
}
.empty,
.loading-row {
  text-align: center;
  color: #777;
  padding: 24px;
}
.table-wrapper::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}
.table-wrapper::-webkit-scrollbar-track {
  background: #f1f1f1;
}
.table-wrapper::-webkit-scrollbar-thumb {
  background: #ccc;
  border-radius: 4px;
}
.table-wrapper::-webkit-scrollbar-thumb:hover {
  background: #aaa;
}
</style>
