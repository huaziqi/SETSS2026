<script setup lang="ts">
import { ref } from 'vue'
import BaseButton from '@/components/BaseButton.vue'
import BaseInput from '@/components/BaseInput.vue'

const username = ref('')
const password = ref('')

const handleLogin = () => {
  console.log('登录信息：', {
    username: username.value,
    password: password.value
  })
}
</script>

<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-header">
        <h1 class="title">Welcome Back</h1>
        <p class="subtitle">请登录你的账户</p>
      </div>

      <div class="login-form">
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

        <div class="options">
          <label class="remember">
            <input type="checkbox" />
            <span>记住我</span>
          </label>

          <a href="#" class="forgot">忘记密码？</a>
        </div>

        <BaseButton mode="dark" size="large" @click="handleLogin">
          登录
        </BaseButton>
      </div>

      <div class="login-footer">
        <span>还没有账号？</span>
        <a href="#">立即注册</a>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f7f7f7;
  padding: 24px;
}

.login-card {
  width: 100%;
  max-width: 420px;
  background: white;
  border: 1.5px solid black;
  border-radius: 20px;
  padding: 40px 32px;
  box-sizing: border-box;
}

.login-header {
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

.login-form {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.options {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
  margin-top: -2px;
}

.remember {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #333;
  cursor: pointer;
}

.remember input {
  width: 14px;
  height: 14px;
  accent-color: black;
}

.forgot {
  color: black;
  text-decoration: none;
}

.forgot:hover {
  text-decoration: underline;
}

.login-footer {
  margin-top: 24px;
  text-align: center;
  font-size: 14px;
  color: #666;
}

.login-footer a {
  margin-left: 6px;
  color: black;
  text-decoration: none;
  font-weight: 500;
}

.login-footer a:hover {
  text-decoration: underline;
}
</style>