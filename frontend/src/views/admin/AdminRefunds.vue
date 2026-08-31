<template>
  <div class="admin-refunds">
    <div class="page-header">
      <h2>退款管理</h2>
      <el-select v-model="statusFilter" placeholder="全部状态" clearable style="width: 140px" @change="fetchList">
        <el-option label="待处理" value="PENDING" />
        <el-option label="已通过" value="APPROVED" />
        <el-option label="已拒绝" value="REJECTED" />
        <el-option label="已退款" value="REFUNDED" />
      </el-select>
    </div>

    <el-table :data="records" stripe v-loading="loading" style="width: 100%">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="orderId" label="订单ID" width="90" />
      <el-table-column prop="userId" label="用户ID" width="90" />
      <el-table-column prop="reason" label="退款原因" min-width="160" show-overflow-tooltip />
      <el-table-column prop="amount" label="金额" width="100">
        <template #default="{ row }">¥{{ row.amount }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusTag(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="adminNote" label="管理员备注" min-width="140" show-overflow-tooltip />
      <el-table-column prop="createTime" label="申请时间" width="170" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <template v-if="row.status === 'PENDING'">
            <el-button size="small" type="success" @click="handleApprove(row)">通过</el-button>
            <el-button size="small" type="danger" @click="handleReject(row)">拒绝</el-button>
          </template>
          <el-button v-if="row.status === 'APPROVED'" size="small" type="primary" @click="handleConfirmRefunded(row)">确认退款</el-button>
          <span v-else-if="row.status !== 'PENDING'" class="text-muted">—</span>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrap" v-if="total > 0">
      <el-pagination
        v-model:current-page="page"
        :page-size="size"
        :total="total"
        layout="prev, pager, next, total"
        @current-change="fetchList"
      />
    </div>

    <!-- Note Dialog -->
    <el-dialog v-model="noteDialogVisible" title="备注" width="400px">
      <el-input v-model="noteText" placeholder="可选备注" />
      <template #footer>
        <el-button @click="noteDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmNoteAction">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminRefundsApi, approveRefundApi, rejectRefundApi, confirmRefundedApi } from '@/api/admin'
import type { Refund } from '@/api/admin'

const records = ref<Refund[]>([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const statusFilter = ref('')

// Note dialog state
const noteDialogVisible = ref(false)
const noteText = ref('')
const pendingAction = ref<{ type: 'approve' | 'reject'; id: number } | null>(null)

onMounted(() => fetchList())

async function fetchList() {
  loading.value = true
  try {
    const res = await getAdminRefundsApi({ page: page.value, size: size.value, status: statusFilter.value || undefined })
    records.value = res.records
    total.value = res.total
  } catch { /* ignore */ }
  loading.value = false
}

function handleApprove(row: Refund) {
  noteText.value = ''
  pendingAction.value = { type: 'approve', id: row.id }
  noteDialogVisible.value = true
}

function handleReject(row: Refund) {
  noteText.value = ''
  pendingAction.value = { type: 'reject', id: row.id }
  noteDialogVisible.value = true
}

async function confirmNoteAction() {
  if (!pendingAction.value) return
  try {
    if (pendingAction.value.type === 'approve') {
      await approveRefundApi(pendingAction.value.id, noteText.value || undefined)
      ElMessage.success('已通过退款申请')
    } else {
      await rejectRefundApi(pendingAction.value.id, noteText.value || undefined)
      ElMessage.success('已拒绝退款申请')
    }
    noteDialogVisible.value = false
    fetchList()
  } catch { /* ignore */ }
}

async function handleConfirmRefunded(row: Refund) {
  try {
    await ElMessageBox.confirm('确认该退款已到账？', '确认', { type: 'warning' })
    await confirmRefundedApi(row.id)
    ElMessage.success('已标记为已退款')
    fetchList()
  } catch { /* ignore */ }
}

function statusLabel(s: string) {
  const map: Record<string, string> = { PENDING: '待处理', APPROVED: '已通过', REJECTED: '已拒绝', REFUNDED: '已退款' }
  return map[s] || s
}

function statusTag(s: string) {
  const map: Record<string, string> = { PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger', REFUNDED: 'info' }
  return map[s] || 'info'
}
</script>

<style scoped>
.admin-refunds {
  padding: 24px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
  color: var(--text);
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.text-muted {
  color: var(--text-muted);
  font-size: 13px;
}
</style>
