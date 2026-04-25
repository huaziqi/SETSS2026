<script setup lang="ts">
import { reactive } from 'vue'
import BaseButton from '@/components/BaseButton.vue'
import BaseInput from '@/components/BaseInput.vue'

const props = defineProps<{
  user: {
    userName?: string
    email?: string
    phone?: string
  }
}>()

const emit = defineEmits<{
  cancel: []
}>()

const form = reactive({
  userName: props.user.userName || '',
  email: props.user.email || '',
  phone: props.user.phone || ''
})

const submit = () => {
  console.log('submit profile:', form)
}
</script>

<template>
  <section class="edit-section">
    <div class="section-title">
      <h2>Edit Profile</h2>
      <p>Update your personal information.</p>
    </div>

    <div class="edit-form">
      <BaseInput
        v-model="form.userName"
        label="Username"
        placeholder="Enter username"
      />

      <BaseInput
        v-model="form.email"
        label="Email"
        placeholder="Enter email"
      />

      <BaseInput
        v-model="form.phone"
        label="Phone"
        placeholder="Enter phone"
      />

      <div class="actions">
        <BaseButton mode="dark" size="medium" @click="submit">
          Save Changes
        </BaseButton>

        <BaseButton mode="light" size="medium" @click="emit('cancel')">
          Cancel
        </BaseButton>
      </div>
    </div>
  </section>
</template>

<style scoped>
.edit-section {
  display: grid;
  grid-template-columns: 260px 1fr;
  gap: 64px;
  padding: 42px 0;
  border-bottom: 1px solid #ddd;
}

.section-title h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.section-title p {
  margin: 8px 0 0;
  color: #777;
  font-size: 14px;
}

.edit-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 8px;
}

@media (max-width: 720px) {
  .edit-section {
    grid-template-columns: 1fr;
    gap: 24px;
  }
}
</style>