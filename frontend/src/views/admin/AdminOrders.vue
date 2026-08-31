<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { Order } from '@/types'
import { getAdminOrdersApi, getAdminOrderDetailApi, updateOrderStatusApi } from '@/api/admin'
import { formatPrice, formatDate, orderStatusMap, statusColorMap } from '@/utils/format'

const orders = ref<Order[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const filterStatus = ref('')
const filterOrderNo = ref('')
const loading = ref(false)

const detailVisible = ref(false)
const detailOrder = ref<Order | null>(null)

const statusOptions = [
  { label: '全部', value: '' },
  { label: '待支付', value: 'PENDING' },
  { label: '已支付', value: 'PAID' },
  { label: '已发货', value: 'SHIPPED' },
  { label: '已收货', value: 'RECEIVED' },
  { label: '已取消', value: 'CANCELLED' },
]

const nextStatusMap: Record<string, { label: string; value: string; type: string }[]> = {
  PENDING: [{ label: '标记已支付', value: 'PAID', type: 'success' }],
  PAID: [{ label: '标记已发货', value: 'SHIPPED', type: 'primary' }],
  SHIPPED: [{ label: '标记已收货', value: 'RECEIVED', type: 'success' }],
}

async function fetchOrders() {
  loading.value = true
  try {
    const params: any = { page: page.value, size: size.value }
    if (filterStatus.value) params.status = filterStatus.value
    if (filterOrderNo.value) params.orderNo = filterOrderNo.value
    const res = await getAdminOrdersApi(params)
    orders.value = res.records
    total.value = res.total
  } catch {
    ElMessage.error('加载订单失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 1
  fetchOrders()
}

function handlePageChange(p: number) {
  page.value = p
  fetchOrders()
}

async function viewDetail(id: number) {
  try {
    detailOrder.value = await getAdminOrderDetailApi(id)
    detailVisible.value = true
  } catch {
    ElMessage.error('加载订单详情失败')
  }
}

const shippingForm = ref({ trackingNo: '', logisticsCompany: '顺丰速运' })
const shippingOrderId = ref(0)
const shippingVisible = ref(false)

function promptShipping(orderId: number) {
  shippingOrderId.value = orderId
  shippingForm.value = { trackingNo: '', logisticsCompany: '顺丰速运' }
  shippingVisible.value = true
}

async function confirmShipping() {
  try {
    await updateOrderStatusApi(shippingOrderId.value, 'SHIPPED', shippingForm.value.trackingNo, shippingForm.value.logisticsCompany)
    ElMessage.success('已发货')
    shippingVisible.value = false
    fetchOrders()
  } catch { ElMessage.error('操作失败') }
}

async function handleUpdateStatus(orderId: number, status: string) {
  if (status === 'SHIPPED') { promptShipping(orderId); return }
  try {
    await updateOrderStatusApi(orderId, status)
    ElMessage.success('状态更新成功')
    fetchOrders()
  } catch {
    ElMessage.error('更新失败')
  }
}

onMounted(() => {
  fetchOrders()
})
</script>

<template>
  <div class="admin-orders">
    <div class="page-header">
      <h2>订单管理</h2>
    </div>

    <div class="filter-bar">
      <el-input v-model="filterOrderNo" placeholder="订单编号" clearable style="width: 220px" @keyup.enter="handleSearch" />
      <el-select v-model="filterStatus" placeholder="订单状态" clearable style="width: 140px">
        <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
      </el-select>
      <el-button @click="handleSearch">搜索</el-button>
    </div>

    <el-table :data="orders" v-loading="loading" border style="width: 100%">
      <el-table-column prop="orderNo" label="订单编号" min-width="180" />
      <el-table-column prop="userId" label="用户ID" width="80" align="center" />
      <el-table-column label="金额" width="120" align="right">
        <template #default="{ row }">{{ formatPrice(row.totalAmount) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="statusColorMap[row.status] || 'info'" size="small">
            {{ orderStatusMap[row.status] || row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="receiverName" label="收货人" width="100" />
      <el-table-column label="下单时间" width="140" align="center">
        <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="250" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="viewDetail(row.id)">详情</el-button>
          <template v-if="nextStatusMap[row.status]">
            <el-button
              v-for="ns in nextStatusMap[row.status]"
              :key="ns.value"
              :type="ns.type as any"
              link
              size="small"
              @click="handleUpdateStatus(row.id, ns.value)"
            >
              {{ ns.label }}
            </el-button>
          </template>
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

    <!-- Order Detail Drawer -->
    <el-drawer v-model="detailVisible" title="订单详情" size="500px">
      <template v-if="detailOrder">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单编号">{{ detailOrder.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="statusColorMap[detailOrder.status] || 'info'" size="small">
              {{ orderStatusMap[detailOrder.status] || detailOrder.status }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="下单用户">{{ detailOrder.userId }}</el-descriptions-item>
          <el-descriptions-item label="订单金额">{{ formatPrice(detailOrder.totalAmount) }}</el-descriptions-item>
          <el-descriptions-item label="收货人">{{ detailOrder.receiverName }}</el-descriptions-item>
          <el-descriptions-item label="电话">{{ detailOrder.receiverPhone }}</el-descriptions-item>
          <el-descriptions-item label="收货地址" :span="2">{{ detailOrder.receiverAddress }}</el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ formatDate(detailOrder.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="支付时间">{{ detailOrder.payTime ? formatDate(detailOrder.payTime) : '-' }}</el-descriptions-item>
        </el-descriptions>

        <h4 style="margin-top: 24px; margin-bottom: 12px">订单商品</h4>
        <el-table :data="detailOrder.orderItems" border size="small">
          <el-table-column prop="productName" label="商品" />
          <el-table-column label="单价" width="90" align="right">
            <template #default="{ row }">{{ formatPrice(row.productPrice) }}</template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="60" align="center" />
          <el-table-column label="小计" width="90" align="right">
            <template #default="{ row }">{{ formatPrice(row.totalPrice) }}</template>
          </el-table-column>
        </el-table>
      </template>
    </el-drawer>

    <!-- 发货弹窗 -->
    <el-dialog v-model="shippingVisible" title="填写物流信息" width="420px">
      <el-form label-width="80px">
        <el-form-item label="物流公司">
          <el-select v-model="shippingForm.logisticsCompany">
            <el-option v-for="c in ['顺丰速运','中通快递','圆通速递','韵达快递','EMS']" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="快递单号" required>
          <el-input v-model="shippingForm.trackingNo" placeholder="请输入快递单号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shippingVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmShipping">确认发货</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.admin-orders { max-width: 1200px; }
.page-header { margin-bottom: 20px; }
.page-header h2 { font-size: 22px; font-weight: 700; color: var(--primary); margin: 0; }
.filter-bar { display: flex; gap: 10px; margin-bottom: 16px; }
.pagination-wrap { display: flex; justify-content: center; margin-top: 24px; }
</style>
