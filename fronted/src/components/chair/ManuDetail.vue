<script setup lang="ts">
import { ref, onMounted, watch } from "vue";
import axios from "axios";

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

const props = defineProps<{
  manuId: number;
}>();

const emit = defineEmits<{
  (e: "back"): void;
  (e: "notify", payload: { type: "success" | "error"; text: string }): void;
}>();

// 状态定义
const loading = ref(false);
const submitting = ref(false);
const manuscript = ref<ManuscriptItem | null>(null);
const reviewers = ref<ReviewerItem[]>([]);

// 分配相关状态
const showAssignModal = ref(false);
const selectedReviewerId = ref<number | null>(null);

// 格式化时间
const formatDate = (dateStr: string) => {
  if (!dateStr) return "-";
  return dateStr.replace("T", " ").substring(0, 19);
};

// 获取稿件详情
const fetchDetails = async () => {
  if (!props.manuId) return;
  loading.value = true;
  try {
    const token = localStorage.getItem("accessToken");
    const response = await axios.get(
      `/api/manuscript/get-single/${props.manuId}`,
      {
        headers: { Authorization: `Bearer ${token}` },
      },
    );

    if (response.data && response.data.code === 200) {
      manuscript.value = response.data.data;
    } else {
      emit("notify", {
        type: "error",
        text: response.data.message || "Failed to load details.",
      });
    }
  } catch (error: any) {
    console.error(error);
    emit("notify", { type: "error", text: "Network error or server issue." });
  } finally {
    loading.value = false;
  }
};

// 加载审稿人列表
const loadReviewers = async () => {
  try {
    const token = localStorage.getItem("accessToken");
    const response = await axios.get("/api/manuscript/chair/assign/reviewer", {
      headers: { Authorization: `Bearer ${token}` },
    });
    if (response.data?.code === 200) {
      reviewers.value = response.data.data || [];
    }
  } catch (error) {
    console.error("Failed to load reviewers", error);
  }
};

// 监听 ID 变化，重新加载数据
watch(
  () => props.manuId,
  () => {
    fetchDetails();
    loadReviewers();
  },
  { immediate: true },
);

// 下载文件
const handleDownload = async () => {
  if (!manuscript.value) return;
  try {
    const token = localStorage.getItem("accessToken");
    const response = await axios.get(
      `/api/manuscript/download/${props.manuId}`,
      {
        responseType: "blob",
        headers: { Authorization: `Bearer ${token}` },
      },
    );

    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement("a");
    link.href = url;
    link.setAttribute(
      "download",
      manuscript.value.originalFileName || "manuscript_file",
    );
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
  } catch (error) {
    emit("notify", { type: "error", text: "Failed to download file." });
  }
};

// 打开分配弹窗
const openAssignModal = () => {
  selectedReviewerId.value = null;
  showAssignModal.value = true;
};

const confirmAssign = async () => {
  if (!selectedReviewerId.value) {
    alert("Please select a reviewer.");
    return;
  }

  submitting.value = true;
  try {
    const token = localStorage.getItem("accessToken");
    const params = new URLSearchParams();
    params.append("manuId", String(props.manuId));
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

      // 🔥 修改点：分配成功后，延迟一小段时间再返回，让用户看到成功提示，然后触发返回
      // 由于 v-if 切换会重新挂载 ManuscriptAssignPanel，其 onMounted 会自动调用 loadData 刷新列表
      setTimeout(() => {
        emit("back");
      }, 800);
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

onMounted(() => {
  loadReviewers();
});
</script>

<template>
  <div class="detail-container">
    <div class="header-actions">
      <button class="back-btn" @click="$emit('back')">← Back to List</button>
      <h2>Manuscript Detail & Assignment</h2>
    </div>

    <div v-if="loading" class="loading-state">Loading details...</div>

    <div v-else-if="manuscript" class="detail-card">
      <div class="info-grid">
        <div class="info-item">
          <label>Title</label>
          <div class="value">{{ manuscript.title }}</div>
        </div>
        <div class="info-item">
          <label>Author</label>
          <div class="value">{{ manuscript.author }}</div>
        </div>
        <div class="info-item full-width">
          <label>Introduction / Abstract</label>
          <div class="value text-block">
            {{ manuscript.introduction || "No introduction provided." }}
          </div>
        </div>
        <div class="info-item">
          <label>Publish Time</label>
          <div class="value">{{ formatDate(manuscript.publishTime) }}</div>
        </div>
        <div class="info-item">
          <label>Status</label>
          <div class="value">
            <span class="status-badge">{{ manuscript.status }}</span>
          </div>
        </div>
      </div>

      <div class="file-section">
        <label>Attached File</label>
        <div class="file-box">
          <span class="file-name"
            >📄 {{ manuscript.originalFileName || "No file" }}</span
          >
          <button
            class="download-btn"
            @click="handleDownload"
            :disabled="!manuscript.originalFileName"
          >
            Download Source
          </button>
        </div>
      </div>

      <div class="action-bar">
        <button class="btn assign-btn" @click="openAssignModal">
          Assign Reviewer
        </button>
      </div>
    </div>

    <!-- 分配弹窗 -->
    <div v-if="showAssignModal" class="modal-overlay">
      <div class="modal-content">
        <h3>Assign Reviewer</h3>
        <p>Select a reviewer for this manuscript:</p>

        <div class="select-wrapper">
          <select v-model="selectedReviewerId">
            <option :value="null" disabled>Select a reviewer...</option>
            <option v-for="rev in reviewers" :key="rev.id" :value="rev.id">
              {{ rev.name }} ({{ rev.email || "No Email" }})
            </option>
          </select>
        </div>

        <div class="modal-actions">
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
  </div>
</template>

<style scoped>
.detail-container {
  padding: 20px;
  max-width: 900px;
  margin: 0 auto;
}
.header-actions {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 20px;
}
.back-btn {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 14px;
  color: #666;
}
.back-btn:hover {
  color: #000;
}
.detail-card {
  background: #fff;
  border: 1px solid #e5e5e5;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}
.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 24px;
}
.info-item.full-width {
  grid-column: span 2;
}
label {
  display: block;
  font-size: 12px;
  color: #666;
  margin-bottom: 4px;
  font-weight: 600;
}
.value {
  font-size: 15px;
  color: #111;
}
.text-block {
  white-space: pre-wrap;
  line-height: 1.6;
  background: #f9f9f9;
  padding: 10px;
  border-radius: 4px;
  border: 1px solid #eee;
}
.file-section {
  margin-bottom: 24px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}
.file-box {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #f0f9ff;
  padding: 12px;
  border-radius: 6px;
  border: 1px solid #bae6fd;
}
.download-btn {
  background: #0284c7;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
}
.download-btn:hover {
  opacity: 0.9;
}
.download-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}
.action-bar {
  display: flex;
  gap: 15px;
  justify-content: flex-end;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}
.btn {
  padding: 10px 24px;
  border-radius: 6px;
  font-weight: 600;
  cursor: pointer;
  border: none;
  transition: opacity 0.2s;
}
.assign-btn {
  background: #2563eb;
  color: white;
}
.assign-btn:hover {
  opacity: 0.9;
}

/* Modal Styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 100;
}
.modal-content {
  background: white;
  padding: 24px;
  border-radius: 8px;
  width: 400px;
  max-width: 90%;
}
.select-wrapper {
  margin: 15px 0;
}
.select-wrapper select {
  width: 100%;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
}
.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
.btn-cancel {
  padding: 8px 16px;
  border: 1px solid #ddd;
  background: white;
  border-radius: 4px;
  cursor: pointer;
}
.btn-confirm {
  padding: 8px 16px;
  background: #2563eb;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
.btn-confirm:disabled {
  background: #93c5fd;
  cursor: not-allowed;
}
</style>
