<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useApi } from '@/utils/useApi'

interface PostItem {
  postId: number
  userId: number
  userName: string
  title: string
  content: string
  tag?: string
  status: string
  commentCount: number
  viewCount: number
  isPinned: boolean
}

const emit = defineEmits<{ (e: 'posts-updated', posts: PostItem[]): void; (e: 'notify', payload: { type: 'success'|'error'; text: string }): void }>()
const { data, fetchData, postData, putData, deleteData } = useApi()

const posts = ref<PostItem[]>([])
const postKeyword = ref('')
const postStatusFilter = ref('ALL')

const postForm = ref({
  postId: null as number | null,
  userId: '',
  title: '',
  content: '',
  tag: '',
  status: 'PUBLISHED',
  isPinned: false
})

const filteredPosts = computed(() =>
  posts.value.filter(post => {
    const keyword = postKeyword.value.trim().toLowerCase()
    const hitKeyword = !keyword ||
      post.title.toLowerCase().includes(keyword) ||
      post.content.toLowerCase().includes(keyword) ||
      (post.userName || '').toLowerCase().includes(keyword) ||
      (post.tag || '').toLowerCase().includes(keyword)
    const hitStatus = postStatusFilter.value === 'ALL' || post.status === postStatusFilter.value
    return hitKeyword && hitStatus
  })
)

const loadPosts = async () => {
  await fetchData('/admin/posts')
  posts.value = data.value?.data || []
  emit('posts-updated', posts.value)
}

const resetPostForm = () => {
  postForm.value = {
    postId: null,
    userId: '',
    title: '',
    content: '',
    tag: '',
    status: 'PUBLISHED',
    isPinned: false
  }
}

const savePost = async () => {
  try {
    const payload = {
      userId: Number(postForm.value.userId),
      title: postForm.value.title,
      content: postForm.value.content,
      tag: postForm.value.tag,
      status: postForm.value.status,
      isPinned: postForm.value.isPinned
    }

    if (postForm.value.postId) {
      await putData(`/admin/posts/${postForm.value.postId}`, payload)
      emit('notify', { type: 'success', text: 'Post updated successfully.' })
    } else {
      await postData('/admin/posts', payload)
      emit('notify', { type: 'success', text: 'Post created successfully.' })
    }

    resetPostForm()
    await loadPosts()
  } catch {
    emit('notify', { type: 'error', text: 'Failed to save post.' })
  }
}

const editPost = (post: PostItem) => {
  postForm.value = {
    postId: post.postId,
    userId: String(post.userId),
    title: post.title,
    content: post.content,
    tag: post.tag || '',
    status: post.status,
    isPinned: !!post.isPinned
  }
}

const removePost = async (postId: number) => {
  if (!confirm('Delete this post and all related comments?')) return
  await deleteData(`/admin/posts/${postId}`)
  emit('notify', { type: 'success', text: 'Post deleted successfully.' })
  await loadPosts()
}

const quickTogglePin = async (post: PostItem) => {
  await putData(`/admin/posts/${post.postId}`, {
    isPinned: !post.isPinned
  })
  await loadPosts()
}

onMounted(loadPosts)
defineExpose({ loadPosts })
</script>

<template>
  <section class="panel">
    <h2>Post Management</h2>
    <div class="toolbar">
      <input v-model="postKeyword" placeholder="Search by title/content/tag/author" />
      <select v-model="postStatusFilter">
        <option value="ALL">All Status</option>
        <option value="PUBLISHED">Published</option>
        <option value="DRAFT">Draft</option>
        <option value="HIDDEN">Hidden</option>
      </select>
      <button @click="resetPostForm">New Post</button>
    </div>

    <div class="form-grid">
      <input v-model="postForm.userId" placeholder="Author User ID" />
      <input v-model="postForm.title" placeholder="Post Title" />
      <input v-model="postForm.tag" placeholder="Tag" />
      <select v-model="postForm.status">
        <option value="PUBLISHED">Published</option>
        <option value="DRAFT">Draft</option>
        <option value="HIDDEN">Hidden</option>
      </select>
      <label><input type="checkbox" v-model="postForm.isPinned" /> Pin Post</label>
      <textarea v-model="postForm.content" rows="4" placeholder="Post Content"></textarea>
      <button @click="savePost">{{ postForm.postId ? 'Update Post' : 'Create Post' }}</button>
    </div>

    <table class="data-table">
      <thead>
        <tr>
          <th>ID</th><th>Title</th><th>Author</th><th>Status</th><th>Tag</th><th>Stats</th><th>Actions</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="post in filteredPosts" :key="post.postId">
          <td>{{ post.postId }}</td>
          <td>{{ post.title }}</td>
          <td>{{ post.userName }} (#{{ post.userId }})</td>
          <td>{{ post.status }}</td>
          <td>{{ post.tag || '-' }}</td>
          <td>{{ post.viewCount }} views · {{ post.commentCount }} comments · {{ post.isPinned ? 'Pinned' : 'Normal' }}</td>
          <td>
            <button @click="editPost(post)">Edit</button>
            <button @click="quickTogglePin(post)">{{ post.isPinned ? 'Unpin' : 'Pin' }}</button>
            <button class="danger" @click="removePost(post.postId)">Delete</button>
          </td>
        </tr>
      </tbody>
    </table>
  </section>
</template>

<style scoped>
.panel { background: #fff; border: 1px solid #e5e5e5; padding: 24px; }
.panel h2 { margin: 0 0 14px; }
.toolbar { display: flex; gap: 10px; margin-bottom: 14px; }
.toolbar input, .toolbar select, .toolbar button { height: 36px; }
.form-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 10px; margin-bottom: 14px; }
textarea { grid-column: 1 / -1; }
input, select, textarea, button { border: 1px solid #ddd; border-radius: 6px; padding: 8px; font-size: 14px; }
button { background: #111; color: #fff; cursor: pointer; }
button.danger { background: #b42323; border-color: #b42323; }
.data-table { width: 100%; border-collapse: collapse; }
.data-table th, .data-table td { border: 1px solid #ececec; padding: 8px; text-align: left; vertical-align: top; }
.data-table th { background: #fafafa; }
</style>
