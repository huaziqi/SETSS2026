<script setup lang="ts">
import { ref } from 'vue'
import BaseButton from '@/components/BaseButton.vue'
import BaseInput from '@/components/BaseInput.vue'
import { useApi } from '../../utils/useApi'

const username = ref('')
const password = ref('')
const confirmPassword = ref('')

const { postData, data, loading, error } = useApi()

const handleRegister = async () => {
  if (!username.value || !password.value || !confirmPassword.value) {
    alert('请填写完整信息')
    return
  }

  if (password.value !== confirmPassword.value) {
    alert('两次输入的密码不一致')
    return
  }

  await postData('/auth/register', {
    username: username.value,
    password: password.value
  })

  console.log(data.value)

  if (!error.value) {
    alert('注册成功')
  }
}
</script>

<template>
  <div class="register-page">
    <div class="register-card">
      <div class="register-header">
        <h1 class="title">Create Account</h1>
        <p class="subtitle">请注册你的账户</p>
      </div>

      <div class="register-form">
        <BaseInput
          v-model="username"
          label="用户名"
          placeholder="请输入用户名"
        />

        <BaseInput
          v-model="password"
          label="密码"
          type="password"
          placeholder="请输入密码"
        />

        <BaseInput
          v-model="confirmPassword"
          label="确认密码"
          type="password"
          placeholder="请再次输入密码"
        />

        <BaseButton mode="dark" size="large" @click="handleRegister">
          注册
        </BaseButton>

        <p v-if="error" class="error-text">
          {{ error }}
        </p>

        <p v-if="loading" class="loading-text">
          正在注册...
        </p>
      </div>

      <div class="register-footer">
        <span>已经有账号？</span>
        <router-link to="/login">登录</router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f7f7f7;
  padding: 24px;
}

.register-card {
  width: 100%;
  max-width: 420px;
  background: white;
  border: 1.5px solid black;
  border-radius: 20px;
  padding: 40px 32px;
  box-sizing: border-box;
}

.register-header {
  text-align: center;
  margin-bottom: 32px;
}

.title {
  margin: 0;
  font-size: 30px;
  font-weight: 700;
  color: black;
  letter-spacing: 0.5px;
}

.subtitle {
  margin: 10px 0 0;
  font-size: 14px;
  color: #666;
}

.register-form {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.register-footer {
  margin-top: 24px;
  text-align: center;
  font-size: 14px;
  color: #666;
}

.register-footer a {
  margin-left: 6px;
  color: black;
  text-decoration: none;
  font-weight: 500;
}

.register-footer a:hover {
  text-decoration: underline;
}

.error-text {
  margin: 0;
  font-size: 13px;
  color: #d93025;
  text-align: center;
}

.loading-text {
  margin: 0;
  font-size: 13px;
  color: #666;
  text-align: center;
}
</style>