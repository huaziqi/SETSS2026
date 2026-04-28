<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import axios from "axios";
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

interface ReviewerItem {
  id: number;
  name: string;
  email?: string;
  role: string;
}

const emit = defineEmits<{
  (e: "notify", payload: { type: "success" | "error"; text: string }): void;
  // 🔥 新增：发射查看详情事件
  (e: "view-detail", id: number): void;
}>();

const { data, fetchData } = useApi();

const manuscripts = ref<ManuscriptItem[]>([]);
const reviewers = ref<ReviewerItem[]>([]);
const keyword = ref("");
const loading = ref(false);
const submitting = ref(false);

// 分配弹窗状态
const showAssignModal = ref(false);
const selectedManuId = ref<number | null>(null);
const selectedReviewerId = ref<number | null>(null);

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
      item.author?.toLowerCase().includes(key),
  );
});

// 加载数据
const loadData = async () => {
  loading.value = true;
  try {
    const token = localStorage.getItem("accessToken");

    // 并行请求稿件和审稿人列表
    const [manuRes, revRes] = await Promise.all([
      axios.get("/api/manuscript/chair/assign/manu", {
        headers: { Authorization: `Bearer ${token}` },
      }),
      axios.get("/api/manuscript/chair/assign/reviewer", {
        headers: { Authorization: `Bearer ${token}` },
      }),
    ]);

    if (manuRes.data?.code === 200) {
      manuscripts.value = manuRes.data.data || [];
    } else {
      emit("notify", {
        type: "error",
        text: manuRes.data?.message || "Failed to load manuscripts.",
      });
    }

    if (revRes.data?.code === 200) {
      reviewers.value = revRes.data.data || [];
    } else {
      emit("notify", {
        type: "error",
        text: revRes.data?.message || "Failed to load reviewers.",
      });
    }
  } catch (error) {
    console.error(error);
    emit("notify", { type: "error", text: "Network error or server issue." });
  } finally {
    loading.value = false;
  }
};

// 打开分配弹窗
const openAssignModal = (manuId: number) => {
  selectedManuId.value = manuId;
  selectedReviewerId.value = null;
  showAssignModal.value = true;
};

// 🔥 修改：点击查看详情时，发射事件给父组件
const handleViewDetail = (manuId: number) => {
  emit("view-detail", manuId);
};

// 确认分配
const confirmAssign = async () => {
  if (!selectedManuId.value || !selectedReviewerId.value) {
    alert("Please select a reviewer.");
    return;
  }

  submitting.value = true;
  try {
    const token = localStorage.getItem("accessToken");
    const params = new URLSearchParams();
    params.append("manuId", String(selectedManuId.value));
    params.append("userId", String(selectedReviewerId.value));

    const response = await axios.post(
      "/api/manuscript/chair/assign/confirm",
      params,
      {
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/x-www-form-urlencoded",
        },
      },
    );

    if (response.data?.code === 200) {
      emit("notify", { type: "success", text: "Assigned successfully!" });
      showAssignModal.value = false;
      loadData(); // 刷新列表
    } else {
      emit("notify", {
        type: "error",
        text: response.data?.message || "Assignment failed.",
      });
    }
  } catch (error: any) {
    emit("notify", {
      type: "error",
      text: error.response?.data?.message || "Network error.",
    });
  } finally {
    submitting.value = false;
  }
};

onMounted(loadData);

defineExpose({ loadData });
</script>

<template>
  <section class="panel">
    <div class="panel-header">
      <h2>Manuscript Assignment</h2>
      <button @click="loadData" :disabled="loading">
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
            <th style="width: 120px">Status</th>
            <th style="width: 180px" class="action-col">Actions</th>
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

            <td>
              <span class="status-badge pending">
                {{ item.status || "AwaitingAssigning" }}
              </span>
            </td>

            <td class="action-col">
              <div class="action-buttons">
                <button
                  class="detail-btn"
                  @click="handleViewDetail(item.manuscriptId)"
                >
                  View Detail
                </button>
                <button
                  class="assign-btn"
                  @click="openAssignModal(item.manuscriptId)"
                >
                  Assign
                </button>
              </div>
            </td>
          </tr>

          <tr v-if="filteredManuscripts.length === 0 && !loading">
            <td colspan="5" class="empty">
              No manuscripts awaiting assignment.
            </td>
          </tr>

          <tr v-if="loading">
            <td colspan="5" class="loading-row">Loading data...</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 分配弹窗 (保持之前优化过的高弹窗样式) -->
    <div v-if="showAssignModal" class="modal-overlay">
      <div class="modal-content tall-modal">
        <div class="modal-header">
          <h3>Assign Reviewer</h3>
          <p class="subtitle">Select a reviewer for this manuscript</p>
        </div>

        <div class="modal-body">
          <div class="select-wrapper">
            <label>Select Reviewer:</label>
            <select v-model="selectedReviewerId">
              <option :value="null" disabled hidden>-- Please Select --</option>
              <option v-for="rev in reviewers" :key="rev.id" :value="rev.id">
                {{ rev.name }} ({{ rev.email || "No Email" }})
              </option>
            </select>
          </div>
        </div>

        <div class="modal-footer">
          <button
            class="btn-cancel"
            @click="showAssignModal = false"
            :disabled="submitting"
          >
            Cancel
          </button>
          <button
            class="btn-confirm"
            @click="confirmAssign"
            :disabled="submitting"
          >
            {{ submitting ? "Assigning..." : "Confirm Assign" }}
          </button>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
/* 保持之前的样式，确保弹窗高且靠上 */
.panel {
  width: 80%;
  margin: 0 auto;
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
.action-buttons {
  display: flex;
  justify-content: center;
  gap: 8px;
}
.assign-btn {
  background: #2563eb;
  color: #fff;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
  transition: opacity 0.2s;
}
.assign-btn:hover {
  opacity: 0.8;
}
.detail-btn {
  background: #fff;
  color: #333;
  border: 1px solid #ddd;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.2s;
}
.detail-btn:hover {
  background: #f5f5f5;
  border-color: #ccc;
}
.empty,
.loading-row {
  text-align: center;
  color: #777;
  padding: 24px;
}

/* Modal Styles - Updated for taller look */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: flex-start; /* 靠顶对齐 */
  padding-top: 40px; /* 距离顶部间距 */
  z-index: 100;
  overflow-y: auto;
}
.modal-content {
  background: white;
  border-radius: 8px;
  width: 450px;
  max-width: 90%;
  display: flex;
  flex-direction: column;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
}
.tall-modal {
  min-height: 350px;
  padding: 0;
  margin-bottom: 40px;
}
.modal-header {
  padding: 24px 24px 10px;
  border-bottom: 1px solid #f0f0f0;
}
.modal-header h3 {
  margin: 0 0 8px;
  font-size: 18px;
  color: #111;
}
.subtitle {
  margin: 0;
  font-size: 13px;
  color: #666;
}
.modal-body {
  padding: 30px 24px;
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.select-wrapper {
  width: 100%;
}
.select-wrapper label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #333;
}
.select-wrapper select {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  background-color: #fff;
  cursor: pointer;
}
.select-wrapper select:focus {
  border-color: #2563eb;
  outline: none;
}
.modal-footer {
  padding: 16px 24px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  background: #fafafa;
  border-radius: 0 0 8px 8px;
}
.btn-cancel {
  padding: 10px 20px;
  border: 1px solid #ddd;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  color: #555;
}
.btn-cancel:hover {
  background: #f5f5f5;
}
.btn-confirm {
  padding: 10px 20px;
  background: #2563eb;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
}
.btn-confirm:hover {
  background: #1d4ed8;
}
.btn-confirm:disabled {
  background: #93c5fd;
  cursor: not-allowed;
}
</style>
