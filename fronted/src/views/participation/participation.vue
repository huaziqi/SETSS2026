<script setup lang="ts">
import { ref } from "vue";
import { useRouter } from "vue-router";
import BaseButton from "@/components/BaseButton.vue";
import SETSSLogo from "@/components/SETSSLogo.vue";

const router = useRouter();

// 状态控制
const showConfirmModal = ref(false);
const showFormModal = ref(false);
const isSubmitting = ref(false);

// 表单数据
const formData = ref({
  fullName: "",
  phone: "",
  email: "",
  affiliation: "", // 单位/学校
  role: "Student", // 身份：Student, Academic, Industry
  remarks: "",
});

// 打开确认弹窗
const handleRegisterFullCourse = () => {
  showConfirmModal.value = true;
};

// 确认报名，进入表单
const confirmAndShowForm = () => {
  showConfirmModal.value = false;
  showFormModal.value = true;
};

// 取消操作
const cancelAction = () => {
  showConfirmModal.value = false;
  showFormModal.value = false;
};

// 提交表单
const submitRegistration = async () => {
  // 简单校验
  if (
    !formData.value.fullName ||
    !formData.value.phone ||
    !formData.value.email
  ) {
    alert("Please fill in all required fields (Name, Phone, Email).");
    return;
  }

  isSubmitting.value = true;

  // TODO: 这里调用后端报名接口
  // await axios.post('/api/register', formData.value) ...

  setTimeout(() => {
    isSubmitting.value = false;
    alert("Registration submitted successfully! (Mock)");
    showFormModal.value = false;
    // 重置表单
    formData.value = {
      fullName: "",
      phone: "",
      email: "",
      affiliation: "",
      role: "Student",
      remarks: "",
    };
  }, 1500);
};

// 报名部分模块（暂未实现）
const handleRegisterModule = () => {
  router.push("/participation/lecture-modules");
};
</script>

<template>
  <div class="participation-page">
    <div class="container">
      <!-- 头部 Logo -->
      <SETSSLogo size="large" subtitle="Participation & Registration" />

      <div class="content-card">
        <!-- 介绍部分 -->
        <section class="intro-section">
          <h1 class="main-title">SETSS 2026 Participation</h1>
          <div class="info-box">
            <p class="date-location">
              <strong>May 11-17, 2026</strong><br />
              Lecture Hall 0114, Building 25, Southwest University, Chongqing,
              China
            </p>
          </div>

          <div class="description">
            <p>
              We are pleased to invite you to participate in the
              <strong
                >8th Spring School on Engineering Trustworthy Software Systems
                (SETSS 2026)</strong
              >.
            </p>
            <p>
              The school will be held at the School of Computer and Information
              Science (CIS), Southwest University (Chongqing, China), and will
              offer lectures on leading-edge research in methods and tools in
              computer system engineering, topical talks on history and trends
              of computing, and a workshop as well.
            </p>
            <p>
              SETSS 2026 is intended for university researchers in computer
              science and technology, including master’s students, Ph.D
              students, academics and software engineering practitioners in
              industry. Participants will gain insight into state-of-the-art
              software engineering methods and technological advances from both
              leading pioneers and outstanding young scholars in the field.
            </p>
          </div>
        </section>

        <hr class="divider" />

        <!-- 报名须知 -->
        <section class="notice-section">
          <h2 class="section-title">Registration Notice</h2>
          <ul class="notice-list">
            <li>Please ensure your personal information is accurate.</li>
            <li>
              Registration for the full course includes access to all lectures
              and workshops.
            </li>
            <li>
              Module registration allows you to select specific topics (Feature
              coming soon).
            </li>
            <li>
              You will receive a confirmation email after successful
              registration.
            </li>
          </ul>
        </section>

        <!-- 操作按钮区域 -->
        <div class="action-area">
          <BaseButton
            mode="dark"
            size="large"
            @click="handleRegisterFullCourse"
            class="btn-register-full"
          >
            Register for Full Course
          </BaseButton>

          <BaseButton
            mode="light"
            size="large"
            @click="handleRegisterModule"
            class="btn-register-module"
          >
            Register for Lecture Modules
          </BaseButton>
        </div>
      </div>
    </div>

    <!-- 1. 确认弹窗 (Confirm Modal) -->
    <div v-if="showConfirmModal" class="modal-overlay">
      <div class="modal-content confirm-modal">
        <h3>Confirm Registration</h3>
        <p>
          You are about to register for the
          <strong>Full SETSS 2026 Course</strong>
          (May 11-17).
        </p>
        <p class="warning-text">
          ⚠️ Please note: Once submitted, you will need to fill in your detailed
          contact information.
        </p>
        <div class="modal-actions">
          <button class="btn-cancel" @click="cancelAction">Cancel</button>
          <button class="btn-confirm" @click="confirmAndShowForm">
            Continue
          </button>
        </div>
      </div>
    </div>

    <!-- 2. 报名表单弹窗 (Form Modal) -->
    <div v-if="showFormModal" class="modal-overlay">
      <div class="modal-content form-modal">
        <div class="form-header">
          <h3>Registration Form</h3>
          <span class="close-icon" @click="cancelAction">&times;</span>
        </div>

        <form @submit.prevent="submitRegistration" class="registration-form">
          <div class="form-group">
            <label>Full Name <span class="required">*</span></label>
            <input
              v-model="formData.fullName"
              type="text"
              placeholder="Enter your full name"
              required
            />
          </div>

          <div class="form-row">
            <div class="form-group half">
              <label>Phone Number <span class="required">*</span></label>
              <input
                v-model="formData.phone"
                type="tel"
                placeholder="+86 1xx xxxx xxxx"
                required
              />
            </div>
            <div class="form-group half">
              <label>Email Address <span class="required">*</span></label>
              <input
                v-model="formData.email"
                type="email"
                placeholder="example@university.edu.cn"
                required
              />
            </div>
          </div>

          <div class="form-group">
            <label>Affiliation (University/Company)</label>
            <input
              v-model="formData.affiliation"
              type="text"
              placeholder="e.g., Southwest University"
            />
          </div>

          <div class="form-group">
            <label>Role</label>
            <select v-model="formData.role">
              <option value="Student">Master/PhD Student</option>
              <option value="Academic">Academic/Researcher</option>
              <option value="Industry">Industry Practitioner</option>
            </select>
          </div>

          <div class="form-group">
            <label>Remarks (Optional)</label>
            <textarea
              v-model="formData.remarks"
              rows="3"
              placeholder="Any dietary requirements or special requests?"
            ></textarea>
          </div>

          <div class="form-actions">
            <button
              type="button"
              class="btn-cancel"
              @click="cancelAction"
              :disabled="isSubmitting"
            >
              Cancel
            </button>
            <button type="submit" class="btn-submit" :disabled="isSubmitting">
              {{ isSubmitting ? "Submitting..." : "Submit Registration" }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<style scoped>
.participation-page {
  min-height: 100vh;
  background: #f7f7f7;
  padding: 40px 20px;
  display: flex;
  justify-content: center;
}

.container {
  width: 100%;
  max-width: 800px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.content-card {
  background: white;
  border-radius: 16px;
  padding: 40px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  border: 1px solid #eee;
}

/* Intro Section */
.main-title {
  font-size: 28px;
  font-weight: 700;
  color: #000;
  margin-bottom: 20px;
  text-align: center;
}

.info-box {
  background: #f9fafb;
  padding: 15px;
  border-radius: 8px;
  text-align: center;
  margin-bottom: 24px;
  border: 1px solid #e5e7eb;
}

.date-location {
  font-size: 16px;
  color: #374151;
  line-height: 1.6;
}

.description p {
  font-size: 15px;
  line-height: 1.8;
  color: #4b5563;
  margin-bottom: 16px;
  text-align: justify;
}

.divider {
  border: none;
  border-top: 1px solid #eee;
  margin: 30px 0;
}

/* Notice Section */
.section-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #111827;
}

.notice-list {
  list-style-type: disc;
  padding-left: 20px;
  color: #4b5563;
  font-size: 14px;
  line-height: 1.8;
}

/* Action Area */
.action-area {
  margin-top: 40px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  align-items: center;
}

.btn-register-full {
  width: 100%;
  max-width: 300px;
}

.btn-register-module {
  width: 100%;
  max-width: 300px;
}

/* Modal Styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  backdrop-filter: blur(2px);
}

.modal-content {
  background: white;
  border-radius: 12px;
  padding: 30px;
  width: 90%;
  max-width: 500px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
  animation: slideUp 0.3s ease-out;
}

@keyframes slideUp {
  from {
    transform: translateY(20px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

/* Confirm Modal Specifics */
.confirm-modal h3 {
  margin-top: 0;
  color: #111827;
}

.warning-text {
  color: #d93025;
  background: #fef2f2;
  padding: 10px;
  border-radius: 6px;
  font-size: 13px;
  margin: 15px 0;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
}

.btn-cancel {
  padding: 8px 16px;
  border: 1px solid #d1d5db;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.btn-cancel:hover {
  background: #f3f4f6;
}

.btn-confirm {
  padding: 8px 16px;
  border: none;
  background: #000;
  color: white;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: opacity 0.2s;
}

.btn-confirm:hover {
  opacity: 0.8;
}

/* Form Modal Specifics */
.form-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}

.form-header h3 {
  margin: 0;
  font-size: 18px;
}

.close-icon {
  font-size: 24px;
  cursor: pointer;
  color: #9ca3af;
}

.close-icon:hover {
  color: #000;
}

.registration-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-row {
  display: flex;
  gap: 16px;
}

.form-group.half {
  flex: 1;
}

label {
  font-size: 13px;
  font-weight: 600;
  color: #374151;
}

.required {
  color: #dc2626;
}

input,
select,
textarea {
  padding: 10px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
}

input:focus,
select:focus,
textarea:focus {
  border-color: #000;
  box-shadow: 0 0 0 2px rgba(0, 0, 0, 0.05);
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 10px;
}

.btn-submit {
  padding: 10px 20px;
  border: none;
  background: #000;
  color: white;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: opacity 0.2s;
}

.btn-submit:hover {
  opacity: 0.8;
}

.btn-submit:disabled {
  background: #9ca3af;
  cursor: not-allowed;
}

@media (max-width: 600px) {
  .form-row {
    flex-direction: column;
    gap: 16px;
  }
  .content-card {
    padding: 24px;
  }
}
</style>
