<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import BaseButton from '@/components/BaseButton.vue'
import BaseInput from '@/components/BaseInput.vue'
import SETSSLogo from '../../components/SETSSLogo.vue'
import { useApi } from '../../utils/useApi'

const username = ref('')
const password = ref('')
const router = useRouter()

const { postData, data, loading, error } = useApi()

const handleLogin = async () => {
  await postData('/api/auth/login', {
    username: username.value,
    password: password.value
  })

  if (!error.value && data.value?.token) {
    localStorage.setItem('accessToken', data.value.token)
    // router.push('/')
  }
}
</script>

<template>
  <div class="login-page">
    <div class="center-box">
      <SETSSLogo size="xlarge" subtitle="Hosted by Southwest University" />

      <div class="login-card">
        <div class="login-header">
          <h1 class="title">Welcome Back</h1>
          <p class="subtitle">Please log in to your account</p>
        </div>

        <div class="login-form">
          <BaseInput
            v-model="username"
            label="Username"
            placeholder="Enter your username"
          />

          <BaseInput
            v-model="password"
            label="Password"
            type="password"
            placeholder="Enter your password"
          />

          <div class="options">
            <label class="remember">
              <input type="checkbox" />
              <span>Remember me</span>
            </label>

            <a href="#" class="forgot">Forgot password?</a>
          </div>

          <BaseButton
            mode="dark"
            size="large"
            :disabled="loading"
            @click="handleLogin"
          >
            {{ loading ? 'Logging in...' : 'Login' }}
          </BaseButton>

          <p v-if="error" class="error-text">
            {{ error }}
          </p>
        </div>

        <div class="login-footer">
          <span>Don't have an account?</span>
          <router-link to="/register">Sign up</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  height: 100vh;
  width: 100%;
  background: #f7f7f7;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 24px;
  box-sizing: border-box;
}

.center-box {
  width: 100%;
  max-width: 420px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 28px;
}

.login-card {
  width: 100%;
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

.error-text {
  margin: -4px 0 0;
  font-size: 13px;
  color: #d93025;
  text-align: center;
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