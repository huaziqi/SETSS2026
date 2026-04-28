<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { useApi } from "@/utils/useApi";

interface Role {
  id: number;
  name: string;
}

interface UserItem {
  id: number;
  name: string;
  email?: string;
  phone?: string;
  enable: boolean;
  roleId?: number;
  roleName?: string;
  manageable?: boolean; // 后端返回的是否可管理标记
}

interface RawUser {
  id: number;
  name: string;
  email?: string;
  phone?: string;
  enable: boolean;
  roleId?: number;
  roleName?: string;
  manageable?: boolean;
}

const emit = defineEmits<{
  (e: "notify", payload: { type: "success" | "error"; text: string }): void;
}>();

const { data, fetchData, postData } = useApi();

const users = ref<UserItem[]>([]);
const keyword = ref("");
const statusFilter = ref("ALL");
const roleFilter = ref("ALL");

// Chair 可以分配的角色列表 (根据实际业务调整，这里假设除了 Admin 和 Chair 以外的角色)
const roleOptions = [
  { id: 1, name: "ROLE_USER" },
  { id: 2, name: "ROLE_ADMIN" }, // 假设 ID 2 是审稿人，请根据数据库实际 ID 调整
  { id: 3, name: "ROLE_REVIEWER" }, // 假设 ID 3 是作者
];

const selectedRoles = ref<Record<number, number | "">>({});

const filteredUsers = computed(() =>
  users.value.filter((user) => {
    const key = keyword.value.trim().toLowerCase();

    const hitKeyword =
      !key ||
      user.name?.toLowerCase().includes(key) ||
      user.email?.toLowerCase().includes(key) ||
      user.phone?.toLowerCase().includes(key) ||
      String(user.id).includes(key);

    const hitStatus =
      statusFilter.value === "ALL" ||
      (statusFilter.value === "ENABLED" && user.enable) ||
      (statusFilter.value === "DISABLED" && !user.enable);

    const roleName = user.roleName || "NO_ROLE";
    const hitRole = roleFilter.value === "ALL" || roleFilter.value === roleName;

    return hitKeyword && hitStatus && hitRole;
  }),
);

const loadUsers = async () => {
  try {
    await fetchData("/api/chair/user/list");
    const rawUsers: RawUser[] = data.value?.data || [];

    users.value = rawUsers.map(
      (rawUser): UserItem => ({
        id: rawUser.id,
        name: rawUser.name,
        email: rawUser.email ?? undefined,
        phone: rawUser.phone ?? undefined,
        enable: rawUser.enable,
        roleId: rawUser.roleId,
        roleName: rawUser.roleName,
        manageable: rawUser.manageable,
      }),
    );

    // 初始化选中角色
    selectedRoles.value = {};
    users.value.forEach((user) => {
      selectedRoles.value[user.id] = user.roleId || "";
    });
  } catch (error) {
    emit("notify", { type: "error", text: "Failed to load users." });
  }
};

const toggleUserStatus = async (user: UserItem) => {
  if (!user.manageable) {
    emit("notify", {
      type: "error",
      text: "This user cannot be managed by Chair.",
    });
    return;
  }

  try {
    if (user.enable) {
      await postData(`/api/chair/user/disable/${user.id}`, {});
      emit("notify", { type: "success", text: "User disabled successfully." });
    } else {
      await postData(`/api/chair/user/enable/${user.id}`, {});
      emit("notify", { type: "success", text: "User enabled successfully." });
    }
    await loadUsers();
  } catch {
    emit("notify", { type: "error", text: "Failed to update user status." });
  }
};

const assignRole = async (user: UserItem) => {
  if (!user.manageable) {
    emit("notify", {
      type: "error",
      text: "This user cannot be managed by Chair.",
    });
    return;
  }

  const roleId = selectedRoles.value[user.id];

  if (!roleId) {
    emit("notify", { type: "error", text: "Please select a role first." });
    return;
  }

  try {
    await postData(
      `/api/chair/user/assignRole?userId=${user.id}&roleId=${roleId}`,
      {},
    );
    emit("notify", { type: "success", text: "Role assigned successfully." });
    await loadUsers();
  } catch {
    emit("notify", { type: "error", text: "Failed to assign role." });
  }
};

onMounted(loadUsers);

defineExpose({ loadUsers });
</script>

<template>
  <section class="panel">
    <div class="panel-header">
      <h2>User Management</h2>
      <button @click="loadUsers">Refresh</button>
    </div>

    <div class="toolbar">
      <input
        v-model="keyword"
        placeholder="Search by ID, name, email, or phone"
      />

      <select v-model="statusFilter">
        <option value="ALL">All Status</option>
        <option value="ENABLED">Enabled</option>
        <option value="DISABLED">Disabled</option>
      </select>

      <select v-model="roleFilter">
        <option value="ALL">All Roles</option>
        <option value="ROLE_USER">User</option>
        <option value="ROLE_ADMIN">Admin</option>
        <option value="ROLE_REVIEWER">Reviewer</option>
        <option value="NO_ROLE">No Role</option>
      </select>
    </div>

    <table class="data-table">
      <thead>
        <tr>
          <th>ID</th>
          <th>Name</th>
          <th>Email</th>
          <th>Phone</th>
          <th>Status</th>
          <th>Current Role</th>
          <th>Assign Role</th>
          <th>Actions</th>
        </tr>
      </thead>

      <tbody>
        <tr v-for="user in filteredUsers" :key="user.id">
          <td>#{{ user.id }}</td>
          <td>
            <strong>{{ user.name }}</strong>
            <span
              v-if="!user.manageable"
              class="lock-hint"
              title="Cannot manage this user"
              >🔒</span
            >
          </td>
          <td>{{ user.email || "-" }}</td>
          <td>{{ user.phone || "-" }}</td>
          <td>
            <span class="status" :class="user.enable ? 'enabled' : 'disabled'">
              {{ user.enable ? "Enabled" : "Disabled" }}
            </span>
          </td>
          <td>{{ user.roleName || "No Role" }}</td>
          <td>
            <div class="role-action">
              <select
                v-model="selectedRoles[user.id]"
                :disabled="!user.manageable"
              >
                <option value="">Select Role</option>
                <option
                  v-for="role in roleOptions"
                  :key="role.id"
                  :value="role.id"
                >
                  {{ role.name }}
                </option>
              </select>
              <button
                class="secondary"
                @click="assignRole(user)"
                :disabled="!user.manageable"
              >
                Save
              </button>
            </div>
          </td>
          <td>
            <button
              :class="user.enable ? 'danger' : 'secondary'"
              @click="toggleUserStatus(user)"
              :disabled="!user.manageable"
            >
              {{ user.enable ? "Disable" : "Enable" }}
            </button>
          </td>
        </tr>

        <tr v-if="filteredUsers.length === 0">
          <td colspan="8" class="empty">No users found.</td>
        </tr>
      </tbody>
    </table>
  </section>
</template>

<style scoped>
.panel {
  background: #fff;
  border: 1px solid #e5e5e5;
  padding: 24px;
}
.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
}
.panel-header h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
}
.toolbar {
  display: flex;
  gap: 10px;
  margin-bottom: 14px;
}
.toolbar input {
  flex: 1;
}
input,
select,
button {
  border: 1px solid #ddd;
  border-radius: 6px;
  padding: 8px 10px;
  font-size: 14px;
}
button {
  background: #111;
  color: #fff;
  cursor: pointer;
  transition: 0.15s ease;
}
button:hover:not(:disabled) {
  opacity: 0.86;
}
button:disabled {
  background: #ccc;
  cursor: not-allowed;
  opacity: 0.6;
}
button.secondary {
  background: #fff;
  color: #111;
  border-color: #bbb;
}
button.secondary:disabled {
  background: #f9f9f9;
  color: #999;
}
button.danger {
  background: #b42323;
  border-color: #b42323;
}
.data-table {
  width: 100%;
  border-collapse: collapse;
}
.data-table th,
.data-table td {
  border: 1px solid #ececec;
  padding: 9px;
  text-align: left;
  vertical-align: top;
  font-size: 14px;
}
.data-table th {
  background: #fafafa;
  font-weight: 600;
}
.role-action {
  display: flex;
  gap: 8px;
}
.status {
  display: inline-block;
  padding: 3px 8px;
  border-radius: 4px;
  font-size: 12px;
}
.status.enabled {
  border: 1px solid #111;
  color: #111;
}
.status.disabled {
  border: 1px solid #b42323;
  color: #b42323;
}
.empty {
  text-align: center;
  color: #777;
  padding: 24px;
}
.lock-hint {
  margin-left: 5px;
  font-size: 12px;
  color: #999;
}
</style>
