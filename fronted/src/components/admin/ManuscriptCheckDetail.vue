<script setup lang="ts">
import { ref, onMounted, watch } from "vue";
import axios from "axios";
import { useApi } from "@/utils/useApi";

const props = defineProps<{
  manuId: number;
}>();

const emit = defineEmits<{
  (e: "back"): void;
  (e: "notify", payload: { type: "success" | "error"; text: string }): void;
}>();

const { validate } = useApi();

// 状态定义
const loading = ref(false);
const submitting = ref(false);
const errorMsg = ref("");
const successMsg = ref("");

// 稿件数据
const manuscript = ref<any>(null);
const rejectReason = ref("");
const showRejectModal = ref(false);

// 获取稿件详情
const fetchDetails = async () => {
  if (!props.manuId) return;

  loading.value = true;
  errorMsg.value = "";
  successMsg.value = "";

  try {
    const token = localStorage.getItem("accessToken");
    const response = await axios.get(
      `/api/manuscript/admin/check-single/${props.manuId}`,
      {
        headers: { Authorization: `Bearer ${token}` },
      },
    );

    if (response.data && response.data.code === 200) {
      manuscript.value = response.data.data;
    } else {
      errorMsg.value = response.data.message || "Failed to load details.";
    }
  } catch (error: any) {
    console.error(error);
    errorMsg.value = "Network error or server issue.";
  } finally {
    loading.value = false;
  }
};

// 监听 ID 变化，重新加载数据
watch(
  () => props.manuId,
  () => {
    fetchDetails();
  },
);

// 下载文件
const handleDownload = async () => {
  if (!manuscript.value) return;

  try {
    const token = localStorage.getItem("accessToken");
    const response = await axios.get(
      `/api/manuscript/admin/check-download/${props.manuId}`,
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

// 通过审核
const handleApprove = async () => {
  if (!manuscript.value) return;
  if (!confirm("Are you sure you want to APPROVE this manuscript?")) return;

  submitting.value = true;
  try {
    const token = localStorage.getItem("accessToken");
    const response = await axios.post(
      `/api/manuscript/admin/approve/${props.manuId}`,
      {},
      {
        headers: { Authorization: `Bearer ${token}` },
      },
    );

    if (response.data && response.data.code === 200) {
      emit("notify", { type: "success", text: "Approved successfully!" });
      emit("back"); // 🔥 返回 list
    } else {
      errorMsg.value = response.data.message || "Approval failed.";
    }
  } catch (error: any) {
    errorMsg.value = error.response?.data?.message || "Network error.";
  } finally {
    submitting.value = false;
  }
};

// 打开拒绝弹窗
const openRejectModal = () => {
  rejectReason.value = "";
  showRejectModal.value = true;
};

// 提交拒绝
const submitReject = async () => {
  if (!rejectReason.value.trim()) {
    alert("Please provide a reason for rejection.");
    return;
  }

  submitting.value = true;
  try {
    const token = localStorage.getItem("accessToken");
    const params = new URLSearchParams();
    params.append("reason", rejectReason.value);

    const response = await axios.post(
      `/api/manuscript/admin/reject/${props.manuId}?${params.toString()}`,
      {},
      { headers: { Authorization: `Bearer ${token}` } },
    );

    if (response.data && response.data.code === 200) {
      emit("notify", { type: "success", text: "Rejected successfully." });
      showRejectModal.value = false;
      emit("back"); // 🔥 返回 list
    } else {
      errorMsg.value = response.data.message || "Rejection failed.";
    }
  } catch (error: any) {
    errorMsg.value = error.response?.data?.message || "Network error.";
  } finally {
    submitting.value = false;
  }
};

onMounted(async () => {
  const isValid = await validate();
  if (!isValid) {
    // 这里无法直接 push 到 login，因为移除了 router
    // 通常 Admin.vue 已经做过校验，这里可以简单处理或 emit 错误
    window.location.href = "/login";
    return;
  }
  fetchDetails();
});
</script>

<template>
  <div class="check-detail-container">
    <div class="header-actions">
      <button class="back-btn" @click="$emit('back')">← Back to List</button>
      <h2>Manuscript Review Detail</h2>
    </div>

    <div v-if="loading" class="loading-state">Loading details...</div>

    <div v-else-if="manuscript" class="detail-card">
      <div v-if="errorMsg" class="alert error">{{ errorMsg }}</div>
      <div v-if="successMsg" class="alert success">{{ successMsg }}</div>

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
          <div class="value">
            {{
              manuscript.publishTime
                ? manuscript.publishTime.replace("T", " ").substring(0, 19)
                : "-"
            }}
          </div>
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
        <button
          class="btn approve"
          @click="handleApprove"
          :disabled="submitting"
        >
          {{ submitting ? "Processing..." : "Approve" }}
        </button>
        <button
          class="btn reject"
          @click="openRejectModal"
          :disabled="submitting"
        >
          Reject / Violation
        </button>
      </div>
    </div>

    <!-- 拒绝原因弹窗 -->
    <div v-if="showRejectModal" class="modal-overlay">
      <div class="modal-content">
        <h3>Reject Manuscript</h3>
        <p>Please provide a reason for rejection or violation:</p>
        <textarea
          v-model="rejectReason"
          rows="4"
          placeholder="Enter reason here..."
        ></textarea>
        <div class="modal-actions">
          <button class="btn-cancel" @click="showRejectModal = false">
            Cancel
          </button>
          <button
            class="btn-confirm"
            @click="submitReject"
            :disabled="submitting"
          >
            {{ submitting ? "Submitting..." : "Confirm Reject" }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.check-detail-container {
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
.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.approve {
  background: #10b981;
  color: white;
}
.approve:hover {
  opacity: 0.9;
}
.reject {
  background: #ef4444;
  color: white;
}
.reject:hover {
  opacity: 0.9;
}
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
.modal-content textarea {
  width: 100%;
  margin: 15px 0;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  resize: vertical;
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
  background: #ef4444;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
.alert {
  padding: 10px;
  border-radius: 4px;
  margin-bottom: 15px;
  font-size: 14px;
}
.alert.error {
  background: #fef2f2;
  color: #b91c1c;
  border: 1px solid #fee2e2;
}
.alert.success {
  background: #f0fdf4;
  color: #15803d;
  border: 1px solid #dcfce7;
}
@media (max-width: 600px) {
  .info-grid {
    grid-template-columns: 1fr;
  }
  .info-item.full-width {
    grid-column: span 1;
  }
  .action-bar {
    flex-direction: column;
  }
  .btn {
    width: 100%;
  }
}
</style>
