<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import axios from "axios";
import BaseButton from "@/components/BaseButton.vue";
import SETSSLogo from "../../components/SETSSLogo.vue";

const router = useRouter();

// 定义组件级别的全局状态 (Ref)
const userId = ref<string>("");
const userToken = ref<string>("");

// 状态定义
const name = ref("");
const title = ref("");
const introduce = ref("");
const file = ref<File | null>(null);

const loading = ref(false);
const errorMsg = ref("");
const successMsg = ref("");

// 返回主页函数
const goBack = () => {
  router.push("/");
};

// 🔥 新增：跳转到我的稿件页面
const goToMyManuscripts = () => {
  router.push("/my-manuscripts");
};

// Token 有效性校验函数
const checkTokenValidity = async (): Promise<boolean> => {
  const idVal = userId.value;
  const tokenVal = userToken.value;

  if (!idVal || !tokenVal) return false;

  try {
    const numericId = parseInt(idVal, 10);
    if (isNaN(numericId)) {
      console.error("Invalid ID format:", idVal);
      return false;
    }

    const response = await axios.post("/api/auth/check-token", null, {
      params: {
        id: numericId,
        token: tokenVal,
      },
    });

    if (response.data && response.data.code === 200) {
      return true;
    } else {
      console.warn("Token check failed:", response.data.message);
      return false;
    }
  } catch (error: any) {
    console.error("Token check error:", error);
    return false;
  }
};

onMounted(async () => {
  const storedToken = localStorage.getItem("accessToken");
  const storedId = localStorage.getItem("id");
  const storedLoginTime = localStorage.getItem("loginTime");

  // 基础非空检查
  if (!storedToken || !storedId) {
    localStorage.clear();
    router.replace("/login");
    return;
  }

  userToken.value = storedToken;
  userId.value = storedId;

  // 时间校验逻辑
  let needsBackendCheck = true; // 默认需要校验

  if (storedLoginTime) {
    const loginTimestamp = new Date(storedLoginTime).getTime();
    const currentTimestamp = Date.now();
    const tenMinutesInMs = 10 * 60 * 1000; // 10分钟 = 600,000毫秒

    // 如果当前时间 - 登录时间 < 10分钟，则不需要向后端请求
    if (currentTimestamp - loginTimestamp < tenMinutesInMs) {
      needsBackendCheck = false;
      console.log(
        "Login session is fresh (< 10 mins). Skipping backend check.",
      );
    } else {
      console.log(
        "Login session expired (> 10 mins). Checking with backend...",
      );
    }
  } else {
    console.log("No login time found. Performing backend check for safety.");
  }

  // 根据标志位决定是否向后端发送请求
  if (needsBackendCheck) {
    const isValid = await checkTokenValidity();

    if (!isValid) {
      localStorage.clear();
      router.replace("/login");
      return; // 校验失败，终止后续操作
    }
  }
});

// 处理文件选择
const handleFileChange = (event: Event) => {
  const target = event.target as HTMLInputElement;
  if (target.files && target.files.length > 0) {
    file.value = target.files[0];
    // 清除之前的错误信息
    errorMsg.value = "";
    successMsg.value = "";
  }
};

// 2. 文件校验逻辑
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
  const maxSize = 50 * 1024 * 1024;

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

// 3. 提交处理
const handleSubmit = async () => {
  errorMsg.value = "";
  successMsg.value = "";

  if (!name.value || !title.value || !introduce.value || !file.value) {
    errorMsg.value = "Please fill in all fields and upload a file.";
    return;
  }

  if (!userToken.value || !userId.value) {
    errorMsg.value = "Session expired. Please login again.";
    router.replace("/login");
    return;
  }

  const validationError = validateFile(file.value);
  if (validationError) {
    errorMsg.value = validationError;
    return;
  }

  loading.value = true;

  try {
    const formData = new FormData();
    formData.append("name", name.value);
    formData.append("id", userId.value);
    formData.append("title", title.value);
    formData.append("introduction", introduce.value);
    formData.append("file", file.value);

    const response = await axios.post("/api/manuscript/submit", formData, {
      headers: {
        Authorization: `Bearer ${userToken.value}`,
      },
    });

    if (response.data && response.data.code === 200) {
      successMsg.value = response.data.message || "Submission successful!";

      // 清空表单
      name.value = "";
      title.value = "";
      introduce.value = "";
      file.value = null;
      const fileInput = document.querySelector(
        ".file-input",
      ) as HTMLInputElement;
      if (fileInput) fileInput.value = "";
    } else {
      errorMsg.value = response.data.message || "Submission failed.";
    }
  } catch (error: any) {
    console.error("Submit error:", error);
    if (error.response) {
      if (error.response.status === 401 || error.response.status === 403) {
        errorMsg.value = "Unauthorized. Please login again.";
        localStorage.clear();
        router.replace("/login");
      } else {
        errorMsg.value =
          error.response.data?.message || "Server error occurred.";
      }
    } else if (error.request) {
      errorMsg.value = "Network error. Please check your connection.";
    } else {
      errorMsg.value = "An unexpected error occurred.";
    }
  } finally {
    loading.value = false;
  }
};
</script>

<template>
  <div class="submit-page">
    <div class="center-box">
      <!-- 顶部 Logo -->
      <SETSSLogo size="large" subtitle="Manuscript Submission" />

      <div class="submit-card">
        <div class="submit-header">
          <h1 class="title">Submit Manuscript</h1>
          <p class="subtitle">Please provide your paper details below</p>
        </div>

        <!-- 🔥 信息提示空间 (Alert Box) -->
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

        <div class="submit-form">
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

          <!-- File Upload -->
          <div class="form-group">
            <label class="input-label"
              >Upload Paper (PDF/Word/MD/Img/Zip)</label
            >
            <div
              class="file-upload-wrapper"
              :class="{ 'is-disabled': loading }"
            >
              <input
                type="file"
                @change="handleFileChange"
                class="file-input"
                accept=".pdf,.docx,.md,.markdown,.jpg,.jpeg,.png,.gif,.zip"
                :disabled="loading"
              />
              <div class="file-name-display" v-if="file">
                📄 {{ file.name }} ({{ (file.size / 1024 / 1024).toFixed(2) }}
                MB)
              </div>
              <div class="file-name-display empty" v-else>No file chosen</div>
            </div>
            <p class="file-hint">
              Max size: 50MB. Formats: .docx, .pdf, .md, images, .zip
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
              {{ loading ? "Submitting..." : "Submit Manuscript" }}
            </BaseButton>

            <!-- 🔥 My Manuscripts Button -->
            <div class="secondary-action">
              <button
                class="link-btn"
                @click="goToMyManuscripts"
                :disabled="loading"
              >
                View My Manuscripts →
              </button>
            </div>
          </div>
        </div>

        <div class="submit-footer">
          <span>Need help?</span>
          <a href="#" class="link">Contact Support</a>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.submit-page {
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

.submit-card {
  width: 100%;
  background: white;
  border: 1.5px solid #000;
  border-radius: 16px;
  padding: 40px;
  box-sizing: border-box;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.04);
}

.submit-header {
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

/* 🔥 Alert Box Styles */
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
.submit-form {
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

/* File Upload */
.file-upload-wrapper {
  position: relative;
  border: 2px dashed #ddd;
  border-radius: 8px;
  padding: 24px;
  text-align: center;
  background: #fafafa;
  transition: all 0.2s;
}

.file-upload-wrapper:hover {
  border-color: #000;
  background: #fff;
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

.file-name-display {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.file-name-display.empty {
  color: #999;
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

.secondary-action {
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

.submit-footer {
  margin-top: 32px;
  text-align: center;
  font-size: 13px;
  color: #666;
  border-top: 1px solid #f0f0f0;
  padding-top: 20px;
}

.submit-footer .link {
  margin-left: 6px;
  color: black;
  text-decoration: none;
  font-weight: 500;
}

.submit-footer .link:hover {
  text-decoration: underline;
}

@media (max-width: 600px) {
  .form-row {
    flex-direction: column;
    gap: 20px;
  }

  .submit-card {
    padding: 24px;
  }
}
</style>
