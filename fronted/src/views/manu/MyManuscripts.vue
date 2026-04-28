<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import axios from "axios";

interface Manuscript {
  manuscriptId: number;
  title: string;
  author: string;
  introduction?: string;
  originalFileName?: string;
  publishTime: string;
  updateTime: string;
  status: string;
}

const router = useRouter();

const loading = ref(false);
const manuscripts = ref<Manuscript[]>([]);
const errorMsg = ref("");

const formatDate = (dateStr: string) => {
  if (!dateStr) return "-";
  return dateStr.replace("T", " ").substring(0, 19);
};

const isEditable = (status: string) => {
  return status === "AwaitingChecking" || status.includes("待审核");
};

// 🔥 新增：判断是否可以查看详情
const canViewDetail = (status: string) => {
  return (
    status === "已评审" ||
    status === "判定违规" ||
    status === "Reviewed" ||
    status === "NonCompliant"
  );
};

const fetchManuscripts = async () => {
  const userId = localStorage.getItem("id");
  const token = localStorage.getItem("accessToken");

  if (!userId || !token) {
    router.replace("/login");
    return;
  }

  loading.value = true;
  errorMsg.value = "";

  try {
    const response = await axios.get(`/api/manuscript/user/${userId}`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });

    if (response.data && response.data.code === 200) {
      manuscripts.value = response.data.data || [];
    } else {
      errorMsg.value = response.data.message || "Failed to load manuscripts.";
    }
  } catch (error: any) {
    console.error("Fetch error:", error);
    if (error.response?.status === 401 || error.response?.status === 403) {
      localStorage.clear();
      router.replace("/login");
    } else {
      errorMsg.value = "Network error or server issue.";
    }
  } finally {
    loading.value = false;
  }
};

const handleDelete = async (item: Manuscript) => {
  if (!confirm(`Are you sure you want to delete "${item.title}"?`)) return;

  const token = localStorage.getItem("accessToken");
  if (!token) {
    router.replace("/login");
    return;
  }

  try {
    const response = await axios.delete(
      `/api/manuscript/delete/${item.manuscriptId}`,
      {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      },
    );

    if (response.data && response.data.code === 200) {
      alert("Deleted successfully.");
      fetchManuscripts();
    } else {
      errorMsg.value = response.data.message || "Failed to delete manuscript.";
    }
  } catch (error: any) {
    console.error("Delete error:", error);
    if (error.response?.status === 401 || error.response?.status === 403) {
      localStorage.clear();
      router.replace("/login");
    } else {
      errorMsg.value =
        error.response?.data?.message ||
        "Network error or server issue during deletion.";
    }
  }
};

const handleUpdate = (item: Manuscript) => {
  sessionStorage.setItem("manuscript_update_data", JSON.stringify(item));

  router.push({
    name: "ManuUpdate",
    params: { id: String(item.manuscriptId) },
  });
};

// 🔥 新增：处理查看详情
const handleViewDetail = (item: Manuscript) => {
  router.push({
    name: "ManuDetail",
    params: { id: String(item.manuscriptId) },
  });
};

const goBack = () => {
  router.push("/submit");
};

onMounted(() => {
  fetchManuscripts();
});
</script>

<template>
  <div class="my-manuscripts-page">
    <div class="container">
      <div class="header-actions">
        <button class="back-btn" @click="goBack">← Back to Submit</button>
        <h1 class="page-title">My Manuscripts</h1>
      </div>

      <div v-if="errorMsg" class="alert-error">
        {{ errorMsg }}
      </div>

      <div v-if="loading" class="loading-state">
        Loading your manuscripts...
      </div>

      <div v-else-if="manuscripts.length === 0" class="empty-state">
        <p>No manuscripts found.</p>
        <button class="create-btn" @click="goBack">
          Submit New Manuscript
        </button>
      </div>

      <div v-else class="table-wrapper">
        <table class="manuscript-table">
          <thead>
            <tr>
              <th>Title</th>
              <th>Author</th>
              <th>Status</th>
              <th>Publish Time</th>
              <th>Update Time</th>
              <th class="action-col">Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(item, index) in manuscripts" :key="index">
              <td class="col-title">{{ item.title }}</td>
              <td>{{ item.author }}</td>
              <td>
                <span :class="['status-badge', item.status.toLowerCase()]">
                  {{ item.status }}
                </span>
              </td>
              <td>{{ formatDate(item.publishTime) }}</td>
              <td>{{ formatDate(item.updateTime) }}</td>
              <td class="col-action">
                <template v-if="isEditable(item.status)">
                  <button
                    class="action-link update"
                    @click="handleUpdate(item)"
                  >
                    Update
                  </button>
                  <span class="separator">|</span>
                  <button
                    class="action-link delete"
                    @click="handleDelete(item)"
                  >
                    Delete
                  </button>
                </template>
                <template v-else-if="canViewDetail(item.status)">
                  <button
                    class="action-link view"
                    @click="handleViewDetail(item)"
                  >
                    View Detail
                  </button>
                </template>
                <span v-else class="no-action">-</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<style scoped>
.my-manuscripts-page {
  min-height: 100vh;
  background: #f7f7f7;
  padding: 40px 20px;
  display: flex;
  justify-content: center;
}

.container {
  width: 100%;
  max-width: 1000px;
  background: white;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 30px;
  border-bottom: 1px solid #eee;
  padding-bottom: 20px;
}

.back-btn {
  background: none;
  border: none;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  transition: color 0.2s;
}

.back-btn:hover {
  color: #000;
}

.page-title {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
}

.alert-error {
  background: #fef2f2;
  color: #d93025;
  padding: 12px;
  border-radius: 6px;
  margin-bottom: 20px;
  border: 1px solid #fee2e2;
}

.loading-state,
.empty-state {
  text-align: center;
  padding: 40px;
  color: #666;
}

.create-btn {
  margin-top: 15px;
  padding: 8px 16px;
  background: #000;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

.table-wrapper {
  overflow-x: auto;
}

.manuscript-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

.manuscript-table th {
  text-align: left;
  padding: 12px 16px;
  background: #fafafa;
  color: #333;
  font-weight: 600;
  border-bottom: 2px solid #eee;
}

.manuscript-table td {
  padding: 12px 16px;
  border-bottom: 1px solid #eee;
  color: #555;
  vertical-align: middle;
}

.col-title {
  font-weight: 500;
  color: #000;
  max-width: 250px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.action-col {
  text-align: center;
  width: 120px;
}

.col-action {
  text-align: center;
}

.status-badge {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.pending,
.status-badge.awaitingchecking {
  background: #fff7ed;
  color: #c2410c;
}
.status-badge.published,
.status-badge.approved,
.status-badge.reviewed {
  background: #f0fdf4;
  color: #15803d;
}
.status-badge.rejected,
.status-badge.noncompliant {
  background: #fef2f2;
  color: #b91c1c;
}

.action-link {
  background: none;
  border: none;
  font-size: 13px;
  cursor: pointer;
  padding: 2px 4px;
  transition: opacity 0.2s;
}

.action-link.update {
  color: #2563eb;
}

.action-link.delete {
  color: #dc2626;
}

/* 🔥 新增：查看详情按钮样式 */
.action-link.view {
  color: #10b981;
}

.action-link:hover {
  text-decoration: underline;
  opacity: 0.8;
}

.separator {
  color: #ddd;
  margin: 0 4px;
}

.no-action {
  color: #ccc;
}

@media (max-width: 768px) {
  .container {
    padding: 20px;
  }

  .manuscript-table th,
  .manuscript-table td {
    padding: 8px 10px;
    font-size: 12px;
  }
}
</style>
