<script setup lang="ts">
import { ref } from 'vue'
import BaseButton from '@/components/BaseButton.vue'
import BaseInput from '@/components/BaseInput.vue'
import SETSSLogo from '@/components/SETSSLogo.vue'
import { useApi } from '../../utils/useApi'

const username = ref('')
const email = ref('')
const phone = ref('')
const password = ref('')
const confirmPassword = ref('')

const { postData, data, loading, error } = useApi()

const handleRegister = async () => {
  if (!username.value || !email.value || !phone.value || !password.value || !confirmPassword.value) {
    alert('Please fill in all fields')
    return
  }

  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  const phoneRegex = /^1[3-9]\d{9}$/

  if (!emailRegex.test(email.value)) {
    alert('Invalid email format')
    return
  }

  if (!phoneRegex.test(phone.value)) {
    alert('Invalid phone number')
    return
  }

  if (password.value !== confirmPassword.value) {
    alert('Passwords do not match')
    return
  }

  await postData('/api/auth/register', {
    username: username.value,
    email: email.value,
    phone: phone.value,
    password: password.value
  })

  console.log(data.value)

  if (!error.value) {
    alert('Registration successful')
  }
}
</script>

<template>
  <div class="register-page">
    <div class="center-box">
      <SETSSLogo size="xlarge" subtitle="Hosted by Southwest University" />

      <div class="register-card">
        <div class="register-header">
          <h1 class="title">Create Account</h1>
          <p class="subtitle">Please create your account</p>
        </div>

        <div class="register-form">
          <BaseInput v-model="username" label="Username" placeholder="Enter your username" />
          <BaseInput v-model="email" label="Email" placeholder="Enter your email" />
          <BaseInput v-model="phone" label="Phone" placeholder="Enter your phone" />
          <BaseInput v-model="password" label="Password" type="password" placeholder="Enter your password" />
          <BaseInput v-model="confirmPassword" label="Confirm Password" type="password" placeholder="Re-enter your password" />

          <BaseButton mode="dark" size="large" :disabled="loading" @click="handleRegister">
            {{ loading ? 'Registering...' : 'Sign Up' }}
          </BaseButton>

          <p v-if="error" class="error-text">{{ error }}</p>
        </div>

        <div class="register-footer">
          <span>Already have an account?</span>
          <router-link to="/login">Login</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.register-page {
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

.register-card {
  width: 100%;
  background: white;
  border: 1.5px solid black;
  border-radius: 20px;
  padding: 36px 32px;
  box-sizing: border-box;
}

.register-header {
  text-align: center;
  margin-bottom: 28px;
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
  gap: 16px;
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
  margin: -4px 0 0;
  font-size: 13px;
  color: #d93025;
  text-align: center;
}
</style>