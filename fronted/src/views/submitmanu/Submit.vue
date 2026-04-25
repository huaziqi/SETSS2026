<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import axios from "axios"; // 🔥 1. 引入 axios
import BaseButton from "@/components/BaseButton.vue";
import SETSSLogo from "../../components/SETSSLogo.vue";

const router = useRouter();

// 状态定义
const name = ref("");
const title = ref("");
const introduce = ref("");
const file = ref<File | null>(null);

const loading = ref(false);
const errorMsg = ref("");
const successMsg = ref("");

// 登录检查
onMounted(() => {
  const token = localStorage.getItem("token");
  const id = localStorage.getItem("id");
  if (!token || !id) {
    router.replace("/login");
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

// 🔥 2. 文件校验逻辑
const validateFile = (file: File): string | null => {
  // 允许的后缀名
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
  // 最大大小 50MB (50 * 1024 * 1024 bytes)
  const maxSize = 50 * 1024 * 1024;

  const fileName = file.name.toLowerCase();
  const isValidExt = allowedExtensions.some((ext) => fileName.endsWith(ext));

  if (!isValidExt) {
    return "Invalid file format. Only .docx, .pdf, .md, images, and .zip are allowed.";
  }

  if (file.size > maxSize) {
    return "File size exceeds 50MB limit.";
  }

  return null; // 校验通过
};

// 🔥 3. 提交处理
const handleSubmit = async () => {
  // 重置消息
  errorMsg.value = "";
  successMsg.value = "";

  // 1. 基础非空校验
  if (!name.value || !title.value || !introduce.value || !file.value) {
    errorMsg.value = "Please fill in all fields and upload a file.";
    return;
  }

  // 2. 获取用户信息
  const token = localStorage.getItem("token");
  const userId = localStorage.getItem("id");

  if (!token || !userId) {
    errorMsg.value = "Session expired. Please login again.";
    router.replace("/login");
    return;
  }

  // 3. 文件校验
  const validationError = validateFile(file.value);
  if (validationError) {
    errorMsg.value = validationError;
    return;
  }

  loading.value = true;

  try {
    // 4. 构建 FormData
    const formData = new FormData();
    formData.append("name", name.value);
    formData.append("id", userId);
    formData.append("title", title.value);
    formData.append("introduction", introduce.value);
    formData.append("file", file.value);

    // 5. 发送请求

    const response = await axios.post("/api/manuscript/submit", formData, {
      headers: {
        Authorization: `Bearer ${token}`,
        // 注意：不要手动设置 Content-Type: multipart/form-data，axios 会自动处理 boundary
      },
    });

    // 6. 处理响应
    // 假设后端返回结构为 Result { code: 200, message: "...", data: ... }
    if (response.data && response.data.code === 200) {
      successMsg.value = response.data.message || "Submission successful!";

      // 🔥 7. 清空表单
      name.value = "";
      title.value = "";
      introduce.value = "";
      file.value = null;

      // 重置文件输入框显示（可选，通过重新渲染或操作 DOM，这里简单置空 file 即可更新 UI）
      const fileInput = document.querySelector(
        ".file-input",
      ) as HTMLInputElement;
      if (fileInput) fileInput.value = "";
    } else {
      // 后端返回业务错误 (code != 200)
      errorMsg.value = response.data.message || "Submission failed.";
    }
  } catch (error: any) {
    console.error("Submit error:", error);

    // 处理 HTTP 错误或网络错误
    if (error.response) {
      // 服务器返回了状态码 (如 401, 403, 500)
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

        <div class="submit-form">
          <!-- 第一行：Name 和 Title 并排 -->
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

          <!-- Introduction Textarea -->
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

          <!-- Error Message -->
          <p v-if="errorMsg" class="error-text">
            {{ errorMsg }}
          </p>

          <!-- Success Message -->
          <p v-if="successMsg" class="success-text">
            {{ successMsg }}
          </p>

          <!-- Submit Button -->
          <BaseButton
            mode="dark"
            size="large"
            :disabled="loading"
            @click="handleSubmit"
            class="submit-btn"
          >
            {{ loading ? "Submitting..." : "Submit Manuscript" }}
          </BaseButton>
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
/* 样式保持不变，仅增加少量提示样式 */
.submit-page {
  min-height: 100vh;
  width: 100%;
  background: #f7f7f7;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
  box-sizing: border-box;
}

.center-box {
  width: 100%;
  max-width: 700px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.submit-card {
  width: 100%;
  background: white;
  border: 1.5px solid black;
  border-radius: 16px;
  padding: 32px 40px;
  box-sizing: border-box;
}

.submit-header {
  text-align: center;
  margin-bottom: 24px;
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

.submit-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-row {
  display: flex;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-group.half {
  flex: 1;
}

.input-label {
  font-size: 13px;
  font-weight: 500;
  color: #333;
  margin-left: 2px;
}

.custom-input,
.custom-textarea {
  width: 100%;
  padding: 10px 14px;
  font-size: 14px;
  border: 1px solid #ddd;
  border-radius: 6px;
  outline: none;
  transition: border-color 0.2s;
  font-family: inherit;
  box-sizing: border-box;
  background: #fafafa;
}

.custom-input:focus,
.custom-textarea:focus {
  border-color: #000;
  background: #fff;
}

.custom-input:disabled,
.custom-textarea:disabled {
  background: #eee;
  cursor: not-allowed;
}

.custom-textarea {
  resize: vertical;
  min-height: 80px;
}

.file-upload-wrapper {
  position: relative;
  border: 1px dashed #ccc;
  border-radius: 6px;
  padding: 16px;
  text-align: center;
  background: #fafafa;
  transition: all 0.2s;
}

.file-upload-wrapper:hover {
  border-color: #000;
  background: #fff;
}

.file-upload-wrapper.is-disabled {
  background: #eee;
  border-color: #ddd;
  cursor: not-allowed;
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

.file-input:disabled {
  cursor: not-allowed;
}

.file-name-display {
  font-size: 13px;
  color: #333;
  font-weight: 500;
}

.file-name-display.empty {
  color: #999;
}

.file-hint {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
  text-align: center;
}

.error-text {
  margin: -5px 0 0;
  font-size: 13px;
  color: #d93025;
  text-align: center;
  font-weight: 500;
}

.success-text {
  margin: -5px 0 0;
  font-size: 13px;
  color: #0f9d58;
  text-align: center;
  font-weight: 500;
}

.submit-btn {
  margin-top: 8px;
  width: 100%;
}

.submit-footer {
  margin-top: 20px;
  text-align: center;
  font-size: 13px;
  color: #666;
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
    gap: 16px;
  }

  .submit-card {
    padding: 24px 20px;
  }
}
</style>
