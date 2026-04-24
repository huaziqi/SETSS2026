<script setup lang="ts">
import { ref } from "vue";
import { useRouter } from "vue-router";
import BaseButton from "@/components/BaseButton.vue";
import SETSSLogo from "../../components/SETSSLogo.vue";

const router = useRouter();

// 表单数据
const name = ref("");
const title = ref("");
const introduce = ref("");
const file = ref<File | null>(null);

// 模拟提交状态
const loading = ref(false);
const error = ref("");
const success = ref("");

// 处理文件选择
const handleFileChange = (event: Event) => {
  const target = event.target as HTMLInputElement;
  if (target.files && target.files.length > 0) {
    file.value = target.files[0];
  }
};

// 提交处理
const handleSubmit = async () => {
  // 简单校验
  if (!name.value || !title.value || !introduce.value || !file.value) {
    error.value = "Please fill in all fields and upload a file.";
    return;
  }

  loading.value = true;
  error.value = "";
  success.value = "";

  // 模拟 API 调用延迟
  setTimeout(() => {
    console.log("Submitting:", {
      name: name.value,
      title: title.value,
      introduce: introduce.value,
      fileName: file.value?.name,
    });

    loading.value = false;
    success.value = "Submission successful! (Mock)";

    // 可选：成功后跳转
    // router.push('/')
  }, 1500);
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
              />
            </div>

            <div class="form-group half">
              <label class="input-label">Paper Title</label>
              <input
                v-model="title"
                type="text"
                class="custom-input"
                placeholder="Enter the title of your paper"
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
            ></textarea>
          </div>

          <!-- File Upload -->
          <div class="form-group">
            <label class="input-label">Upload Paper (PDF/Word)</label>
            <div class="file-upload-wrapper">
              <input
                type="file"
                @change="handleFileChange"
                class="file-input"
                accept=".pdf,.doc,.docx"
              />
              <div class="file-name-display" v-if="file">
                📄 {{ file.name }}
              </div>
              <div class="file-name-display empty" v-else>No file chosen</div>
            </div>
          </div>

          <!-- Error Message -->
          <p v-if="error" class="error-text">
            {{ error }}
          </p>

          <!-- Success Message -->
          <p v-if="success" class="success-text">
            {{ success }}
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
/* 页面背景布局 */
.submit-page {
  min-height: 100vh;
  width: 100%;
  background: #f7f7f7;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px; /* 减小外层padding */
  box-sizing: border-box;
}

.center-box {
  width: 100%;
  max-width: 700px; /* 🔥 关键：加宽容器，让形状变扁 */
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px; /* 减小Logo和卡片的间距 */
}

/* 卡片样式 */
.submit-card {
  width: 100%;
  background: white;
  border: 1.5px solid black;
  border-radius: 16px; /* 稍微减小圆角，显得更利落 */
  padding: 32px 40px; /* 🔥 关键：减小上下padding(32px)，增加左右padding(40px) */
  box-sizing: border-box;
}

.submit-header {
  text-align: center;
  margin-bottom: 24px; /* 减小头部下方间距 */
}

.title {
  margin: 0;
  font-size: 28px; /* 稍微减小标题字号 */
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
  gap: 16px; /* 减小表单元素间距 */
}

/* 🔥 新增：横向排列的行 */
.form-row {
  display: flex;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

/* 🔥 关键：让两个输入框各占一半宽度 */
.form-group.half {
  flex: 1;
}

.input-label {
  font-size: 13px; /* 稍微减小标签字号 */
  font-weight: 500;
  color: #333;
  margin-left: 2px;
}

/* 自定义 Input 样式 */
.custom-input,
.custom-textarea {
  width: 100%;
  padding: 10px 14px; /* 稍微减小内边距，让输入框看起来更紧凑 */
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

.custom-textarea {
  resize: vertical;
  min-height: 80px; /* 限制最小高度 */
}

/* 文件上传样式 */
.file-upload-wrapper {
  position: relative;
  border: 1px dashed #ccc;
  border-radius: 6px;
  padding: 16px; /* 减小内边距 */
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
  font-size: 13px;
  color: #333;
  font-weight: 500;
}

.file-name-display.empty {
  color: #999;
}

/* 错误与成功提示 */
.error-text {
  margin: -5px 0 0;
  font-size: 13px;
  color: #d93025;
  text-align: center;
}

.success-text {
  margin: -5px 0 0;
  font-size: 13px;
  color: #0f9d58;
  text-align: center;
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

/* 响应式适配：屏幕变窄时恢复单列 */
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
