<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import axios from "axios";
import BaseButton from "@/components/BaseButton.vue";
import SETSSLogo from "../../components/SETSSLogo.vue";

const router = useRouter();
const route = useRoute();

// 从路由参数获取 ID
const manuscriptId = Number(route.params.id);

// 表单数据
const name = ref("");
const title = ref("");
const introduce = ref("");
const originalFileName = ref<string>(""); // 后端返回的原文件名

// 文件相关状态
const file = ref<File | null>(null);
const isFileChanged = ref(false); // 标记用户是否替换了文件

// 通用状态
const loading = ref(false);
const errorMsg = ref("");
const successMsg = ref("");
const userToken = ref<string>("");
const userId = ref<string>("");

// ManuUpdate.vue

onMounted(() => {
  const storedToken = localStorage.getItem("accessToken");
  const storedId = localStorage.getItem("id");

  if (!storedToken || !storedId) {
    router.replace("/login");
    return;
  }

  userToken.value = storedToken;
  userId.value = storedId;

  // 🔥 从 sessionStorage 获取数据
  const cachedDataStr = sessionStorage.getItem("manuscript_update_data");
  let passedData = null;

  if (cachedDataStr) {
    try {
      passedData = JSON.parse(cachedDataStr);
      // 获取后可以选择清除，也可以保留直到更新成功
      sessionStorage.removeItem("manuscript_update_data");
    } catch (e) {
      console.error("Failed to parse cached data", e);
    }
  }

  console.log("[Debug] Cached Data from SessionStorage:", passedData);
  console.log("[Debug] Route Param ID:", manuscriptId);

  // 比较 ID
  if (passedData && Number(passedData.manuscriptId) === manuscriptId) {
    console.log("✅ Using cached data from sessionStorage.");
    fillFormWithData(passedData);
  } else {
    console.log("⚠️ No cache or ID mismatch. Fetching from backend...");
    fetchManuscriptDetails();
  }
});

// 提取填充表单的逻辑，复用代码
const fillFormWithData = (data: any) => {
  name.value = data.author;
  title.value = data.title;
  introduce.value = data.introduction || ""; // 防止 null
  originalFileName.value = data.originalFileName || "";
};

// 1. 获取稿件详细信息 (仅在无缓存时调用)
const fetchManuscriptDetails = async () => {
  loading.value = true;
  try {
    const response = await axios.get(`/api/manuscript/update/${manuscriptId}`, {
      headers: {
        Authorization: `Bearer ${userToken.value}`,
      },
    });

    if (response.data && response.data.code === 200) {
      const data = response.data.data;
      fillFormWithData(data);
    } else {
      errorMsg.value =
        response.data.message || "Failed to load manuscript details.";
    }
  } catch (error: any) {
    console.error("Fetch details error:", error);
    handleError(error);
  } finally {
    loading.value = false;
  }
};

// 2. 下载原文件
const handleDownloadOriginal = async () => {
  try {
    // 使用 axios 获取 blob 流
    const response = await axios.get(
      `/api/manuscript/update/download/${manuscriptId}`,
      {
        responseType: "blob", // 关键：指定响应类型为 blob
        headers: {
          Authorization: `Bearer ${userToken.value}`,
        },
      },
    );

    // 创建下载链接
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement("a");
    link.href = url;
    // 尝试从 header 获取文件名，或者使用后端返回的名字
    link.setAttribute("download", originalFileName.value || "manuscript_file");
    document.body.appendChild(link);
    link.click();

    // 清理
    link.remove();
    window.URL.revokeObjectURL(url);
  } catch (error: any) {
    console.error("Download error:", error);
    errorMsg.value = "Failed to download original file.";
  }
};

// 3. 处理新文件选择
const handleFileChange = (event: Event) => {
  const target = event.target as HTMLInputElement;
  if (target.files && target.files.length > 0) {
    const newFile = target.files[0];

    // 校验文件
    const validationError = validateFile(newFile);
    if (validationError) {
      errorMsg.value = validationError;
      // 清空输入，防止无效文件被选中
      target.value = "";
      return;
    }

    file.value = newFile;
    isFileChanged.value = true; // 标记文件已更改
    errorMsg.value = ""; // 清除之前的错误
  }
};

// 文件校验逻辑 (复用 Submit2 的逻辑)
const validateFile = (file: File): string | null => {
  const allowedExtensions = [
    ".docx",
    ".pdf",
    ".md",
    ".markdown",
    ".jpg",
    ".jpeg",
    ".png",
    ".gif",
    ".zip",
  ];
  const maxSize = 50 * 1024 * 1024; // 50MB

  const fileName = file.name.toLowerCase();
  const isValidExt = allowedExtensions.some((ext) => fileName.endsWith(ext));

  if (!isValidExt) {
    return "Invalid file format. Only .docx, .pdf, .md, images, and .zip are allowed.";
  }

  if (file.size > maxSize) {
    return "File size exceeds 50MB limit.";
  }

  return null;
};

// 4. 提交更新
const handleSubmit = async () => {
  errorMsg.value = "";
  successMsg.value = "";

  if (!name.value || !title.value || !introduce.value) {
    errorMsg.value = "Please fill in all text fields.";
    return;
  }

  // 如果文件被更改，再次校验（虽然选择时校验过，但双重保险）
  if (isFileChanged.value && file.value) {
    const validationError = validateFile(file.value);
    if (validationError) {
      errorMsg.value = validationError;
      return;
    }
  }

  loading.value = true;

  try {
    const formData = new FormData();
    formData.append("name", name.value);
    formData.append("id", userId.value);
    formData.append("title", title.value);
    formData.append("introduction", introduce.value);

    // 关键逻辑：只有当文件被更改时，才添加 file 字段
    // 后端接口定义: @RequestParam(value = "file", required = false) MultipartFile file
    if (isFileChanged.value && file.value) {
      formData.append("file", file.value);
    }

    const response = await axios.post(
      `/api/manuscript/update/submit/${manuscriptId}`,
      formData,
      {
        headers: {
          Authorization: `Bearer ${userToken.value}`,
          // 注意：发送 FormData 时不要手动设置 Content-Type，浏览器会自动设置 boundary
        },
      },
    );

    if (response.data && response.data.code === 200) {
      successMsg.value = "Manuscript updated successfully!";
      // 可选：延迟跳转回列表页
      setTimeout(() => {
        router.push("/my-manuscripts");
      }, 1500);
    } else {
      errorMsg.value = response.data.message || "Update failed.";
    }
  } catch (error: any) {
    console.error("Update error:", error);
    handleError(error);
  } finally {
    loading.value = false;
  }
};

// 错误处理辅助函数
const handleError = (error: any) => {
  if (error.response?.status === 401 || error.response?.status === 403) {
    errorMsg.value = "Session expired. Please login again.";
    localStorage.clear();
    router.replace("/login");
  } else {
    errorMsg.value =
      error.response?.data?.message || "Network error or server issue.";
  }
};

// 导航函数
const goBack = () => {
  router.push("/my-manuscripts");
};
</script>

<template>
  <div class="update-page">
    <div class="center-box">
      <!-- 顶部 Logo -->
      <SETSSLogo size="large" subtitle="Update Manuscript" />

      <div class="update-card">
        <div class="update-header">
          <h1 class="title">Edit Manuscript</h1>
          <p class="subtitle">Modify your paper details below</p>
        </div>

        <!-- 提示信息 -->
        <transition name="fade">
          <div v-if="errorMsg" class="alert-box error">
            <span class="icon">⚠️</span>
            <span>{{ errorMsg }}</span>
          </div>
        </transition>

        <transition name="fade">
          <div v-if="successMsg" class="alert-box success">
            <span class="icon">✅</span>
            <span>{{ successMsg }}</span>
          </div>
        </transition>

        <div class="update-form" v-if="!loading">
          <!-- Name & Title -->
          <div class="form-row">
            <div class="form-group half">
              <label class="input-label">Author Name</label>
              <input
                v-model="name"
                type="text"
                class="custom-input"
                placeholder="Enter your full name"
                :disabled="loading"
              />
            </div>

            <div class="form-group half">
              <label class="input-label">Paper Title</label>
              <input
                v-model="title"
                type="text"
                class="custom-input"
                placeholder="Enter the title of your paper"
                :disabled="loading"
              />
            </div>
          </div>

          <!-- Introduction -->
          <div class="form-group">
            <label class="input-label">Abstract / Introduction</label>
            <textarea
              v-model="introduce"
              class="custom-textarea"
              placeholder="Briefly describe your work..."
              rows="4"
              :disabled="loading"
            ></textarea>
          </div>

          <!-- File Section (Distinct from Submit) -->
          <div class="form-group">
            <label class="input-label">Current File</label>

            <div class="file-status-container">
              <!-- 情况1: 用户没有选择新文件，显示原文件信息和下载按钮 -->
              <div v-if="!isFileChanged" class="original-file-box">
                <div class="file-info">
                  <span class="icon">📄</span>
                  <span class="filename">{{
                    originalFileName || "No file attached"
                  }}</span>
                </div>
                <button
                  v-if="originalFileName"
                  class="download-btn"
                  @click="handleDownloadOriginal"
                  :disabled="loading"
                >
                  ⬇ Download Original
                </button>
              </div>

              <!-- 情况2: 用户选择了新文件，显示新文件信息 -->
              <div v-else class="new-file-box">
                <div class="file-info highlight">
                  <span class="icon">✨</span>
                  <span class="filename">{{ file?.name }}</span>
                  <span class="size"
                    >({{
                      (file?.size ? file.size / 1024 / 1024 : 0).toFixed(2)
                    }}
                    MB)</span
                  >
                </div>
                <button
                  class="revert-btn"
                  @click="
                    () => {
                      file = null;
                      isFileChanged = false;
                    }
                  "
                >
                  ↺ Revert to Original
                </button>
              </div>
            </div>

            <!-- 上传新文件的区域 -->
            <div class="file-upload-wrapper mt-2">
              <input
                type="file"
                @change="handleFileChange"
                class="file-input"
                accept=".pdf,.docx,.md,.markdown,.jpg,.jpeg,.png,.gif,.zip"
                :disabled="loading"
              />
              <div class="upload-prompt">
                {{
                  isFileChanged
                    ? "Click to replace file"
                    : "Click to upload a new version (optional)"
                }}
              </div>
            </div>
            <p class="file-hint">
              Max size: 50MB. If you don't upload a new file, the original will
              be kept.
            </p>
          </div>

          <!-- Action Buttons Area -->
          <div class="action-area">
            <BaseButton
              mode="dark"
              size="large"
              :disabled="loading"
              @click="handleSubmit"
              class="submit-btn"
            >
              {{ loading ? "Updating..." : "Save Changes" }}
            </BaseButton>

            <div class="secondary-actions">
              <button class="link-btn" @click="goBack" :disabled="loading">
                ← Back to My Manuscripts
              </button>
            </div>
          </div>
        </div>

        <!-- Loading Skeleton or Text -->
        <div v-else class="loading-state">Loading manuscript data...</div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 复用 Submit2 的大部分样式，仅做微调 */
.update-page {
  min-height: 100vh;
  width: 100%;
  background: #f7f7f7;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px 20px;
  box-sizing: border-box;
}

.center-box {
  width: 100%;
  max-width: 720px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
}

.update-card {
  width: 100%;
  background: white;
  border: 1.5px solid #000;
  border-radius: 16px;
  padding: 40px;
  box-sizing: border-box;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.04);
}

.update-header {
  text-align: center;
  margin-bottom: 32px;
}

.title {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  color: black;
  letter-spacing: 0.5px;
}

.subtitle {
  margin: 8px 0 0;
  font-size: 14px;
  color: #666;
}

/* Alert Box Styles */
.alert-box {
  padding: 12px 16px;
  border-radius: 8px;
  margin-bottom: 24px;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 10px;
  animation: slideDown 0.3s ease-out;
}

.alert-box.error {
  background-color: #fef2f2;
  color: #d93025;
  border: 1px solid #fee2e2;
}

.alert-box.success {
  background-color: #f0fdf4;
  color: #0f9d58;
  border: 1px solid #dcfce7;
}

.icon {
  font-size: 16px;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* Form Styles */
.update-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-row {
  display: flex;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group.half {
  flex: 1;
}

.input-label {
  font-size: 13px;
  font-weight: 600;
  color: #333;
}

.custom-input,
.custom-textarea {
  width: 100%;
  padding: 12px 16px;
  font-size: 14px;
  border: 1px solid #ddd;
  border-radius: 8px;
  outline: none;
  transition: all 0.2s;
  font-family: inherit;
  box-sizing: border-box;
  background: #fafafa;
}

.custom-input:focus,
.custom-textarea:focus {
  border-color: #000;
  background: #fff;
  box-shadow: 0 0 0 3px rgba(0, 0, 0, 0.05);
}

.custom-textarea {
  resize: vertical;
  min-height: 100px;
}

/* File Status Container - New Feature */
.file-status-container {
  margin-bottom: 10px;
}

.original-file-box,
.new-file-box {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-radius: 8px;
  background: #f9fafb;
  border: 1px solid #e5e7eb;
}

.new-file-box {
  background: #eff6ff;
  border-color: #bfdbfe;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #374151;
  font-weight: 500;
}

.file-info.highlight {
  color: #1e40af;
}

.filename {
  max-width: 300px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.size {
  font-size: 12px;
  color: #6b7280;
  font-weight: normal;
}

.download-btn,
.revert-btn {
  font-size: 12px;
  padding: 6px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid transparent;
}

.download-btn {
  background: white;
  border-color: #d1d5db;
  color: #374151;
}

.download-btn:hover {
  background: #f3f4f6;
  border-color: #9ca3af;
}

.revert-btn {
  background: transparent;
  color: #dc2626;
}

.revert-btn:hover {
  text-decoration: underline;
}

/* File Upload Wrapper */
.file-upload-wrapper {
  position: relative;
  border: 2px dashed #ddd;
  border-radius: 8px;
  padding: 16px;
  text-align: center;
  background: #fafafa;
  transition: all 0.2s;
}

.file-upload-wrapper:hover {
  border-color: #000;
  background: #fff;
}

.mt-2 {
  margin-top: 8px;
}

.file-input {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  opacity: 0;
  cursor: pointer;
}

.upload-prompt {
  font-size: 13px;
  color: #666;
}

.file-hint {
  font-size: 12px;
  color: #999;
  margin-top: 6px;
  text-align: center;
}

/* Action Area */
.action-area {
  margin-top: 10px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.submit-btn {
  width: 100%;
}

.secondary-actions {
  display: flex;
  justify-content: center;
}

.link-btn {
  background: none;
  border: none;
  color: #666;
  font-size: 14px;
  cursor: pointer;
  text-decoration: none;
  transition: color 0.2s;
  font-weight: 500;
  padding: 8px;
}

.link-btn:hover {
  color: #000;
  text-decoration: underline;
}
.link-btn:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

.loading-state {
  text-align: center;
  padding: 40px;
  color: #666;
}

@media (max-width: 600px) {
  .form-row {
    flex-direction: column;
    gap: 20px;
  }
  .update-card {
    padding: 24px;
  }
  .original-file-box,
  .new-file-box {
    flex-direction: column;
    gap: 10px;
    align-items: flex-start;
  }
  .download-btn,
  .revert-btn {
    width: 100%;
    text-align: center;
  }
}
</style>
