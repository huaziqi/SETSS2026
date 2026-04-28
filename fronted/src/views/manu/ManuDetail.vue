NEW_FILE_CODE
<script setup lang="ts">
import { ref, onMounted, watch, computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import axios from "axios";

const route = useRoute();
const router = useRouter();

const loading = ref(false);
const errorMsg = ref("");
const manuscript = ref<any>(null);

// 获取稿件详情
const fetchDetails = async () => {
  const manuId = route.params.id;
  if (!manuId) return;

  loading.value = true;
  errorMsg.value = "";

  try {
    const token = localStorage.getItem("accessToken");
    const response = await axios.get(
      `/api/manuscript/detail/get-single/${manuId}`,
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

// 判断是否为 NonCompliant 状态
const isNonCompliant = computed(() => {
  return (
    manuscript.value?.status === "NonCompliant" ||
    manuscript.value?.status === "判定违规"
  );
});

// 判断是否为 Reviewed 状态
const isReviewed = computed(() => {
  return (
    manuscript.value?.status === "Reviewed" ||
    manuscript.value?.status === "已评审"
  );
});

watch(
  () => route.params.id,
  () => {
    fetchDetails();
  },
);

const goBack = () => {
  router.push("/my-manuscripts");
};

onMounted(() => {
  fetchDetails();
});
</script>

<template>
  <div class="manu-detail-page">
    <div class="container">
      <div class="header-actions">
        <button class="back-btn" @click="goBack">← Back to List</button>
        <h1 class="page-title">Manuscript Detail</h1>
      </div>

      <div v-if="loading" class="loading-state">Loading details...</div>

      <div v-else-if="errorMsg" class="alert-error">{{ errorMsg }}</div>

      <div v-else-if="manuscript" class="detail-card">
        <!-- 基本信息 -->
        <div class="info-section">
          <h2>Basic Information</h2>
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
                <span
                  :class="['status-badge', manuscript.status.toLowerCase()]"
                >
                  {{ manuscript.status }}
                </span>
              </div>
            </div>
          </div>
        </div>

        <!-- NonCompliant 状态：显示违规原因 -->
        <div v-if="isNonCompliant" class="review-section violation-section">
          <h2>Violation Reason</h2>
          <div class="reason-box">
            {{ manuscript.reviewResult || "No reason provided." }}
          </div>
        </div>

        <!-- Reviewed 状态：显示审稿信息 -->
        <div v-else-if="isReviewed" class="review-section">
          <h2>Review Information</h2>

          <div class="info-grid">
            <div class="info-item" v-if="manuscript.reviewer">
              <label>Reviewer</label>
              <div class="value">{{ manuscript.reviewer }}</div>
            </div>

            <div class="info-item" v-if="manuscript.grade">
              <label>Grade</label>
              <div class="value">
                <span class="grade-badge">{{ manuscript.grade }}</span>
              </div>
            </div>
          </div>

          <div class="review-result-section">
            <label>Review Result</label>
            <div class="result-content">
              {{ manuscript.reviewResult || "No review result provided." }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.manu-detail-page {
  min-height: 100vh;
  background: #f7f7f7;
  padding: 40px 20px;
  display: flex;
  justify-content: center;
}

.container {
  width: 100%;
  max-width: 900px;
  background: white;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 30px;
  border-bottom: 1px solid #eee;
  padding-bottom: 20px;
}

.back-btn {
  background: none;
  border: none;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  transition: color 0.2s;
}

.back-btn:hover {
  color: #000;
}

.page-title {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
}

.loading-state {
  text-align: center;
  padding: 40px;
  color: #666;
}

.alert-error {
  background: #fef2f2;
  color: #d93025;
  padding: 12px;
  border-radius: 6px;
  margin-bottom: 20px;
  border: 1px solid #fee2e2;
}

.detail-card {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.info-section,
.review-section {
  margin-bottom: 30px;
}

.info-section h2,
.review-section h2 {
  margin: 0 0 20px;
  font-size: 18px;
  font-weight: 600;
  color: #111;
  padding-bottom: 10px;
  border-bottom: 2px solid #f0f0f0;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
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
  padding: 12px;
  border-radius: 6px;
  border: 1px solid #eee;
}

.status-badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 500;
}

.status-badge.reviewed {
  background: #f0fdf4;
  color: #15803d;
  border: 1px solid #dcfce7;
}

.status-badge.noncompliant {
  background: #fef2f2;
  color: #b91c1c;
  border: 1px solid #fee2e2;
}

.violation-section {
  padding-top: 20px;
  border-top: 2px solid #f0f0f0;
}

.reason-box {
  background: #fef2f2;
  border: 1px solid #fee2e2;
  border-left: 4px solid #dc2626;
  padding: 16px;
  border-radius: 6px;
  line-height: 1.6;
  color: #991b1b;
}

.review-result-section {
  margin-top: 20px;
}

.result-content {
  white-space: pre-wrap;
  line-height: 1.8;
  background: #fafafa;
  padding: 16px;
  border-radius: 6px;
  border: 1px solid #e5e5e5;
  font-size: 14px;
}

.grade-badge {
  display: inline-block;
  padding: 6px 16px;
  border-radius: 6px;
  font-size: 16px;
  font-weight: 700;
  background: #dbeafe;
  color: #1e40af;
  border: 2px solid #bfdbfe;
}

@media (max-width: 768px) {
  .container {
    padding: 20px;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .info-item.full-width {
    grid-column: span 1;
  }
}
</style>
