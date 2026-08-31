<template>
  <div class="admin-reviews">
    <div class="page-header"><h2>评价管理</h2></div>

    <el-table :data="records" stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column label="商品ID" width="80">
        <template #default="{ row }">{{ row.productId }}</template>
      </el-table-column>
      <el-table-column label="用户ID" width="80">
        <template #default="{ row }">{{ row.userId }}</template>
      </el-table-column>
      <el-table-column label="评分" width="100">
        <template #default="{ row }">
          <span class="stars">{{ '★'.repeat(row.rating) }}{{ '☆'.repeat(5 - row.rating) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip />
      <el-table-column prop="createTime" label="时间" width="160" />
      <el-table-column label="操作" width="80" align="center">
        <template #default="{ row }">
          <el-popconfirm title="确定删除该评价？" @confirm="handleDelete(row.id)">
            <template #reference>
              <el-button type="danger" size="small" link>删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrap" v-if="total > 0">
      <el-pagination v-model:current-page="page" :page-size="size" :total="total"
        layout="prev, pager, next, total" @current-change="fetchList" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/api/request'
import type { Review } from '@/api/review'
import type { PageData } from '@/types'

const records = ref<Review[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const loading = ref(false)

onMounted(() => fetchList())

async function fetchList() {
  loading.value = true
  try {
    const res = await request.get<any, PageData<Review>>('/reviews/admin/list', { params: { page: page.value, size: size.value } })
    records.value = res.records
    total.value = res.total
  } catch { /* */ }
  loading.value = false
}

async function handleDelete(id: number) {
  try {
    await request.delete('/reviews/' + id)
    ElMessage.success('已删除')
    fetchList()
  } catch { ElMessage.error('删除失败') }
}
</script>

<style scoped>
.admin-reviews { padding: 24px; }
.page-header { margin-bottom: 24px; }
.page-header h2 { margin: 0; font-size: 22px; font-weight: 600; }
.stars { color: #f5a623; letter-spacing: 1px; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 20px; }
</style>
