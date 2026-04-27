<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import axios from "axios";
import BaseButton from "@/components/BaseButton.vue";
import SETSSLogo from "@/components/SETSSLogo.vue";
import { useApi } from "@/utils/useApi";

const router = useRouter();
const { validate } = useApi();

// 定义 DTO 接口
interface LectureModule {
  id: number;
  moduleNo: number;
  lecturer: string;
  courseTopic: string;
  introduction: string;
  exactTime: string;
  price: number;
  selected: boolean; // 用于前端勾选状态
}

// 状态定义
const modules = ref<LectureModule[]>([]);
const loading = ref(false);
const errorMsg = ref("");

// 获取讲座模块列表
const fetchModules = async () => {
  loading.value = true;
  errorMsg.value = "";

  // 简单的鉴权检查
  const isValid = await validate();
  if (!isValid) {
    router.replace("/login");
    return;
  }

  try {
    const token = localStorage.getItem("accessToken");
    const response = await axios.get("/api/participation/lecture-modules", {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });

    if (response.data) {
      // 假设后端直接返回 List<LectureModuleDTO>
      // 如果后端返回的是 Result 包装类，请改为 response.data.data
      modules.value = response.data.map((item: any) => ({
        ...item,
        selected: false, // 初始化选中状态为 false
      }));
    }
  } catch (error: any) {
    console.error("Fetch modules error:", error);
    if (error.response?.status === 401 || error.response?.status === 403) {
      localStorage.clear();
      router.replace("/login");
    } else {
      errorMsg.value =
        "Failed to load lecture modules. Please try again later.";
    }
  } finally {
    loading.value = false;
  }
};

// 切换选中状态
const toggleSelection = (module: LectureModule) => {
  module.selected = !module.selected;
};

// 提交报名（暂未实现具体接口）
const handleSubmitSelection = () => {
  const selectedModules = modules.value.filter((m) => m.selected);

  if (selectedModules.length === 0) {
    alert("Please select at least one lecture module.");
    return;
  }

  const ids = selectedModules.map((m) => m.id);
  console.log("Selected Module IDs:", ids);

  // TODO: 调用后端报名接口
  // await axios.post('/api/participation/register-modules', { moduleIds: ids })

  alert(
    `Successfully registered for ${selectedModules.length} module(s)! (Mock)`,
  );
};

// 返回上一页
const goBack = () => {
  router.push("/participation");
};

onMounted(() => {
  fetchModules();
});
</script>

<template>
  <div class="lecture-modules-page">
    <div class="container">
      <!-- 头部 -->
      <div class="header-actions">
        <button class="back-btn" @click="goBack">
          ← Back to Participation
        </button>
        <h1 class="page-title">Lecture Modules Registration</h1>
      </div>

      <!-- 错误提示 -->
      <div v-if="errorMsg" class="alert-error">
        {{ errorMsg }}
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-state">Loading lecture modules...</div>

      <!-- 模块列表 -->
      <div v-else-if="modules.length > 0" class="modules-grid">
        <div
          v-for="module in modules"
          :key="module.id"
          class="module-card"
          :class="{ 'is-selected': module.selected }"
          @click="toggleSelection(module)"
        >
          <div class="card-header">
            <span class="module-no">Module {{ module.moduleNo }}</span>
            <div class="check-box">
              <svg
                v-if="module.selected"
                viewBox="0 0 24 24"
                fill="none"
                class="check-icon"
              >
                <path
                  d="M20 6L9 17L4 12"
                  stroke="white"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
              </svg>
            </div>
          </div>

          <h3 class="course-topic">{{ module.courseTopic }}</h3>

          <div class="lecturer-info">
            <span class="label">Lecturer:</span>
            <span class="value">{{ module.lecturer }}</span>
          </div>

          <div class="time-info">
            <span class="label">Time:</span>
            <span class="value">{{ module.exactTime }}</span>
          </div>

          <p class="introduction">{{ module.introduction }}</p>

          <div class="card-footer">
            <span class="price">¥{{ module.price }}</span>
            <span class="status-text">
              {{ module.selected ? "Selected" : "Click to Select" }}
            </span>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-else-if="!loading" class="empty-state">
        <p>No lecture modules available at the moment.</p>
      </div>

      <!-- 底部操作栏 -->
      <div v-if="modules.length > 0" class="action-bar">
        <BaseButton
          mode="dark"
          size="large"
          @click="handleSubmitSelection"
          :disabled="loading"
        >
          Confirm Selection
        </BaseButton>
      </div>
    </div>
  </div>
</template>

<style scoped>
.lecture-modules-page {
  min-height: 100vh;
  background: #f7f7f7;
  padding: 40px 20px;
  display: flex;
  justify-content: center;
}

.container {
  width: 100%;
  max-width: 1000px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* Header */
.header-actions {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 10px;
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

/* Alerts & States */
.alert-error {
  background: #fef2f2;
  color: #d93025;
  padding: 12px;
  border-radius: 6px;
  border: 1px solid #fee2e2;
}

.loading-state,
.empty-state {
  text-align: center;
  padding: 40px;
  color: #666;
  background: white;
  border-radius: 12px;
}

/* Grid Layout */
.modules-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

/* Module Card */
.module-card {
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
  display: flex;
  flex-direction: column;
}

.module-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

.module-card.is-selected {
  border-color: #000;
  background: #fafafa;
  box-shadow: 0 0 0 2px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.module-no {
  font-size: 12px;
  font-weight: 600;
  color: #6b7280;
  background: #f3f4f6;
  padding: 4px 8px;
  border-radius: 4px;
}

.check-box {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
}

.is-selected .check-box {
  background: #000;
}

.check-icon {
  width: 14px;
  height: 14px;
}

.course-topic {
  font-size: 18px;
  font-weight: 700;
  color: #111827;
  margin: 0 0 12px 0;
  line-height: 1.4;
}

.lecturer-info,
.time-info {
  font-size: 13px;
  color: #4b5563;
  margin-bottom: 6px;
  display: flex;
  gap: 6px;
}

.label {
  font-weight: 600;
  color: #6b7280;
}

.introduction {
  font-size: 14px;
  color: #6b7280;
  line-height: 1.6;
  margin: 12px 0;
  flex-grow: 1; /* Pushes footer down */
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid #f3f4f6;
}

.price {
  font-size: 16px;
  font-weight: 700;
  color: #000;
}

.status-text {
  font-size: 12px;
  font-weight: 500;
  color: #9ca3af;
}

.is-selected .status-text {
  color: #000;
}

/* Action Bar */
.action-bar {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

@media (max-width: 600px) {
  .modules-grid {
    grid-template-columns: 1fr;
  }
}
</style>
