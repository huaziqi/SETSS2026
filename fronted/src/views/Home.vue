<template>
  <div class="container">
    <h1 class="title">SETSS2026</h1>
    <p class="subtitle">Welcome to the Conference Website</p>

    <button @click="handleClick">点击我</button>
  </div>
</template>

<script setup lang="ts">
const handleClick = () => {
  alert('欢迎来到 SETSS2026 🎉')
}
</script>

<style scoped>
.container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #e3f2fd, #ffffff);
}

.title {
  font-size: 48px;
  font-weight: bold;
  color: #1976d2;
  margin-bottom: 10px;
}

.subtitle {
  font-size: 18px;
  color: #666;
  margin-bottom: 30px;
}

button {
  padding: 10px 20px;
  font-size: 16px;
  border: none;
  border-radius: 8px;
  background-color: #1976d2;
  color: white;
  cursor: pointer;
}

button:hover {
  background-color: #1565c0;
}
</style>