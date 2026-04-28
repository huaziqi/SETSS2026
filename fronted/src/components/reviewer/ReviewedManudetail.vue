NEW_FILE_CODE
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

const loading = ref(false);
const errorMsg = ref("");

const manuscript = ref<any>(null);

const fetchDetails = async () => {
  if (!props.manuId) return;

  loading.value = true;
  errorMsg.value = "";

  try {
    const token = localStorage.getItem("accessToken");
    const response = await axios.get(
      `/api/manuscript/review/get-single/${props.manuId}`,
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

watch(
  () => props.manuId,
  () => {
    fetchDetails();
  },
);

onMounted(async () => {
  const isValid = await validate();
  if (!isValid) {
    window.location.href = "/login";
    return;
  }
  fetchDetails();
});
</script>

<template>
  <div class="reviewed-detail-container">
    <div class="header-actions">
      <button class="back-btn" @click="$emit('back')">← Back to List</button>
      <h2>Reviewed Manuscript Detail</h2>
    </div>

    <div v-if="loading" class="loading-state">Loading details...</div>

    <div v-else-if="manuscript" class="detail-card">
      <div v-if="errorMsg" class="alert error">{{ errorMsg }}</div>

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
          <label>Update Time</label>
          <div class="value">
            {{
              manuscript.updateTime
                ? manuscript.updateTime.replace("T", " ").substring(0, 19)
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
        <div class="info-item" v-if="manuscript.reviewer">
          <label>Reviewer</label>
          <div class="value">{{ manuscript.reviewer }}</div>
        </div>
      </div>

      <div class="review-info-section">
        <h3>Review Information</h3>

        <div class="review-detail-item">
          <label>Grade</label>
          <div class="value">
            <span v-if="manuscript.grade" class="grade-display">{{ manuscript.grade }}</span>
            <span v-else class="no-grade-text">Not graded</span>
          </div>
        </div>

        <div class="review-detail-item">
          <label>Review Result</label>
          <div class="value result-full">
            {{ manuscript.reviewResult || "No review result provided." }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.reviewed-detail-container {
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
.status-badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 500;
  background: #f0fdf4;
  color: #15803d;
  border: 1px solid #dcfce7;
}
.review-info-section {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 2px solid #eee;
}
.review-info-section h3 {
  margin: 0 0 20px;
  font-size: 18px;
  color: #111;
}
.review-detail-item {
  margin-bottom: 20px;
}
.grade-display {
  display: inline-block;
  padding: 6px 16px;
  border-radius: 6px;
  font-size: 16px;
  font-weight: 700;
  background: #dbeafe;
  color: #1e40af;
  border: 2px solid #bfdbfe;
}
.no-grade-text {
  color: #999;
  font-style: italic;
}
.result-full {
  white-space: pre-wrap;
  line-height: 1.8;
  background: #fafafa;
  padding: 16px;
  border-radius: 6px;
  border: 1px solid #e5e5e5;
  font-size: 14px;
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
@media (max-width: 600px) {
  .info-grid {
    grid-template-columns: 1fr;
  }
  .info-item.full-width {
    grid-column: span 1;
  }
}
</style>
