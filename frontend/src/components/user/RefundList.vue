<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getMyRefundsApi } from '@/api/refund'
import type { Refund } from '@/api/admin'
import { formatPrice, formatDate } from '@/utils/format'
import Pagination from '@/components/product/Pagination.vue'

const refunds = ref<Refund[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const loading = ref(false)

const statusMap: Record<string, string> = {
  PENDING: '待审核',
  APPROVED: '已通过',
  REJECTED: '已拒绝',
  REFUNDED: '已退款',
}
const statusTag: Record<string, string> = {
  PENDING: 'warning',
  APPROVED: 'success',
  REJECTED: 'danger',
  REFUNDED: 'info',
}

async function fetchRefunds() {
  loading.value = true
  try {
    const res = await getMyRefundsApi({ page: page.value, size: size.value })
    refunds.value = res.records
    total.value = res.total
  } catch { /* ignore */ } finally {
    loading.value = false
  }
}

function onPageChange(p: number) { page.value = p; fetchRefunds() }
function onSizeChange(s: number) { size.value = s; page.value = 1; fetchRefunds() }

onMounted(() => fetchRefunds())
</script>

<template>
  <div class="refund-list">
    <el-table :data="refunds" v-loading="loading" style="width: 100%"
      :header-cell-style="{ background: '#fafaf8', color: '#2d2d2d', fontWeight: 600 }">
      <el-table-column prop="orderId" label="订单ID" width="80" />
      <el-table-column label="退款金额" width="120" align="right">
        <template #default="{ row }">
          <span class="refund-amount">{{ formatPrice(row.amount) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="reason" label="退款理由" min-width="180" show-overflow-tooltip />
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="statusTag[row.status] || 'info'" size="small">
            {{ statusMap[row.status] || row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="adminNote" label="管理员备注" min-width="140" show-overflow-tooltip>
        <template #default="{ row }">
          {{ row.adminNote || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="申请时间" width="140" align="center">
        <template #default="{ row }">
          {{ formatDate(row.createTime) }}
        </template>
      </el-table-column>
    </el-table>

    <div v-if="refunds.length === 0 && !loading" class="empty-refunds">
      暂无退款记录
    </div>

    <Pagination :total="total" :page="page" :size="size"
      @page-change="onPageChange" @size-change="onSizeChange" />
  </div>
</template>

<style scoped>
.refund-list {
  background: white;
  border-radius: 16px;
  border: 1px solid var(--border);
  overflow: hidden;
}
.refund-amount {
  font-weight: 700;
  color: var(--accent);
}
.empty-refunds {
  text-align: center;
  padding: 64px 24px;
  color: var(--text-muted);
  font-size: 15px;
}
</style>
