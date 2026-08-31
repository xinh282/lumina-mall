<script setup lang="ts">
import { ref, onMounted } from 'vue'
import type { Order } from '@/types'
import { getOrderListApi, cancelOrderApi, confirmReceiptApi } from '@/api/order'
import { applyRefundApi } from '@/api/refund'
import { formatPrice, formatDate, orderStatusMap, statusColorMap } from '@/utils/format'
import { ElMessage, ElMessageBox } from 'element-plus'
import Pagination from '@/components/product/Pagination.vue'

const orders = ref<Order[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const loading = ref(false)

async function fetchOrders() {
  loading.value = true
  try {
    const res = await getOrderListApi({ page: page.value, size: size.value })
    orders.value = res.records
    total.value = res.total
  } catch {
    ElMessage.error('加载订单失败')
  } finally {
    loading.value = false
  }
}

function onPageChange(p: number) {
  page.value = p
  fetchOrders()
}

function onSizeChange(s: number) {
  size.value = s
  page.value = 1
  fetchOrders()
}

async function handleCancel(id: number) {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await cancelOrderApi(id)
    ElMessage.success('订单已取消')
    fetchOrders()
  } catch {
    // user cancelled or error
  }
}

async function handleConfirmReceipt(id: number) {
  try {
    await ElMessageBox.confirm('确认已收到货物？', '确认收货', {
      confirmButtonText: '确认收货',
      cancelButtonText: '取消',
      type: 'info',
    })
    await confirmReceiptApi(id)
    ElMessage.success('已确认收货')
    fetchOrders()
  } catch {
    // user cancelled or error
  }
}

function viewDetail(id: number) {
  ElMessage.info('查看订单详情: ' + id)
}

// Refund
const refundDialogVisible = ref(false)
const refundOrderId = ref(0)
const refundReason = ref('')
const refundSubmitting = ref(false)

function openRefundDialog(orderId: number) {
  refundOrderId.value = orderId
  refundReason.value = ''
  refundDialogVisible.value = true
}

async function handleRefund() {
  if (!refundReason.value.trim()) {
    ElMessage.warning('请填写退款理由')
    return
  }
  refundSubmitting.value = true
  try {
    await applyRefundApi(refundOrderId.value, refundReason.value)
    ElMessage.success('退款申请已提交，请等待审核')
    refundDialogVisible.value = false
    fetchOrders()
  } catch {
    ElMessage.error('退款申请失败')
  } finally {
    refundSubmitting.value = false
  }
}

onMounted(() => {
  fetchOrders()
})
</script>

<template>
  <div class="order-list">
    <el-table :data="orders" v-loading="loading" style="width: 100%" :header-cell-style="{ background: '#fafaf8', color: '#2d2d2d', fontWeight: 600 }">
      <el-table-column prop="orderNo" label="订单编号" min-width="180">
        <template #default="{ row }">
          <span class="order-no">{{ row.orderNo }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="totalAmount" label="金额" width="120" align="right">
        <template #default="{ row }">
          <span class="order-amount">{{ formatPrice(row.totalAmount) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="statusColorMap[row.status] || 'info'" size="small">
            {{ orderStatusMap[row.status] || row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="下单时间" width="130" align="center">
        <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="物流" width="120" align="center">
        <template #default="{ row }">
          <span v-if="row.trackingNo">{{ row.logisticsCompany }} {{ row.trackingNo }}</span>
          <span v-else style="color:#ccc">--</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" align="center">
        <template #default="{ row }">
          <div class="action-btns">
            <el-button type="primary" link size="small" @click="viewDetail(row.id)">
              查看详情
            </el-button>
            <el-button
              v-if="row.status === 'PENDING'"
              type="primary"
              link
              size="small"
              @click="$router.push('/pay?orderId=' + row.id + '&orderNo=' + row.orderNo + '&amount=' + row.totalAmount)"
            >
              去支付
            </el-button>
            <el-button
              v-if="row.status === 'PENDING'"
              type="danger"
              link
              size="small"
              @click="handleCancel(row.id)"
            >
              取消
            </el-button>
            <el-button
              v-if="row.status === 'SHIPPED'"
              type="success"
              link
              size="small"
              @click="handleConfirmReceipt(row.id)"
            >
              确认收货
            </el-button>
            <el-button
              v-if="row.status === 'RECEIVED'"
              type="warning"
              link
              size="small"
              @click="openRefundDialog(row.id)"
            >
              申请退款
            </el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <div v-if="orders.length === 0 && !loading" class="empty-orders">
      暂无订单记录
    </div>

    <Pagination
      :total="total"
      :page="page"
      :size="size"
      @page-change="onPageChange"
      @size-change="onSizeChange"
    />

    <!-- 退款申请弹窗 -->
    <el-dialog v-model="refundDialogVisible" title="申请退款" width="440px">
      <el-form label-width="80px">
        <el-form-item label="退款理由" required>
          <el-input
            v-model="refundReason"
            type="textarea"
            :rows="3"
            placeholder="请描述退款原因，如商品与描述不符、质量问题等"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="refundDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="refundSubmitting" @click="handleRefund">
          提交申请
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.order-list {
  background: white;
  border-radius: 16px;
  border: 1px solid var(--border);
  overflow: hidden;
  padding: 0;
}
.order-no {
  font-family: monospace;
  font-size: 13px;
  color: var(--primary);
}
.order-amount {
  font-weight: 700;
  color: var(--accent);
}
.action-btns {
  display: flex;
  gap: 8px;
  justify-content: center;
}
.empty-orders {
  text-align: center;
  padding: 64px 24px;
  color: var(--text-muted);
  font-size: 15px;
}
</style>
