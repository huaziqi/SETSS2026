<script setup lang="ts">
import Header from '@/layouts/Header.vue'
import PinnedPostSection from '@/layouts/forum/PinnedPostSection.vue'
import PostListSection from '@/layouts/forum/PostListSection.vue'
import ForumSidebar from '@/layouts/forum/ForumSidebar.vue'
</script>

<template>
  <div class="forum-page">
    <Header />

    <main class="forum-main">

      <div class="forum-layout">
        <div class="forum-content">
          <PinnedPostSection />
          <PostListSection />
        </div>

        <ForumSidebar />
      </div>
    </main>
  </div>
</template>

<style scoped>
.forum-page {
  min-height: 100vh;
  background: #f7f7f8;
}

.forum-main {
  max-width: 1180px;
  margin: 0 auto;
  padding: 32px 24px 60px;
}

.forum-hero {
  margin-bottom: 28px;
}

.forum-hero h1 {
  margin: 0;
  font-size: 34px;
  font-weight: 700;
  color: #111;
}

.forum-hero p {
  margin-top: 8px;
  color: #666;
  font-size: 15px;
}

.forum-layout {
  display: flex;
  align-items: flex-start;
  gap: 28px;
}

.forum-content {
  flex: 1;
  min-width: 0;
}

@media (max-width: 900px) {
  .forum-layout {
    flex-direction: column;
  }

  :deep(.forum-sidebar) {
    width: 100%;
  }
}
</style>