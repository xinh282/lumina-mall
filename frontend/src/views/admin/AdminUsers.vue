<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { UserInfo } from '@/types'
import { getAdminUsersApi, updateUserStatusApi } from '@/api/admin'
import { formatDate } from '@/utils/format'

const users = ref<UserInfo[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const loading = ref(false)

async function fetchUsers() {
  loading.value = true
  try {
    const res = await getAdminUsersApi({ page: page.value, size: size.value })
    users.value = res.records
    total.value = res.total
  } catch {
    ElMessage.error('加载用户失败')
  } finally {
    loading.value = false
  }
}

function handlePageChange(p: number) {
  page.value = p
  fetchUsers()
}

async function handleToggleStatus(row: UserInfo, newStatus: number) {
  try {
    await updateUserStatusApi(row.id, newStatus)
    ElMessage.success(newStatus === 1 ? '已启用' : '已禁用')
    fetchUsers()
  } catch {
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  fetchUsers()
})
</script>

<template>
  <div class="admin-users">
    <div class="page-header">
      <h2>用户管理</h2>
    </div>

    <el-table :data="users" v-loading="loading" border style="width: 100%">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="username" label="用户名" min-width="120" />
      <el-table-column prop="nickname" label="昵称" min-width="120" />
      <el-table-column prop="email" label="邮箱" min-width="160" />
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column label="角色" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'info'" size="small">
            {{ row.role === 'ADMIN' ? '管理员' : '用户' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
            {{ row.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="注册时间" width="140" align="center">
        <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="120" align="center" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="row.role !== 'ADMIN'"
            :type="row.status === 1 ? 'danger' : 'success'"
            link
            size="small"
            @click="handleToggleStatus(row, row.status === 1 ? 0 : 1)"
          >
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <span v-else style="color: #ccc; font-size: 12px">-</span>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrap" v-if="total > 0">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        background
        @change="handlePageChange"
      />
    </div>
  </div>
</template>

<style scoped>
.admin-users { max-width: 1200px; }
.page-header { margin-bottom: 20px; }
.page-header h2 { font-size: 22px; font-weight: 700; color: var(--primary); margin: 0; }
.pagination-wrap { display: flex; justify-content: center; margin-top: 24px; }
</style>
