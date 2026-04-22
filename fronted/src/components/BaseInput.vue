<script setup lang="ts">
const model = defineModel<string>()

withDefaults(defineProps<{
  label?: string
  type?: string
  placeholder?: string
}>(), {
  label: '',
  type: 'text',
  placeholder: ''
})
</script>

<template>
  <div class="input-wrapper">
    <label v-if="label" class="input-label">{{ label }}</label>
    <input
      v-model="model"
      class="input"
      :type="type"
      :placeholder="placeholder"
    />
  </div>
</template>

<style scoped>
.input-wrapper {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.input-label {
  font-size: 14px;
  font-weight: 500;
  color: black;
}

.input {
  width: 100%;
  height: 44px;
  box-sizing: border-box;
  border: 1.5px solid black;
  border-radius: 10px;
  padding: 0 14px;
  font-size: 14px;
  background: white;
  color: black;
  outline: none;
  transition: all 0.2s ease;
}

.input::placeholder {
  color: #999;
}

.input:focus {
  background: #fafafa;
}
</style>