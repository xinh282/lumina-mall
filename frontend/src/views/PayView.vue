<template>
  <div class="pay-page">
    <div class="pay-card" v-if="payForm">
      <h2>订单支付</h2>
      <div class="pay-amount">¥{{ orderAmount }}</div>
      <div class="pay-order-no">订单号：{{ orderNo }}</div>
      <div class="pay-form" v-html="payForm"></div>
      <div class="pay-actions">
        <p class="pay-hint">支付完成后点击下方按钮查询状态</p>
        <el-button type="primary" :loading="checkingPayment" @click="checkPayment">查询支付状态</el-button>
        <el-button @click="$router.push('/user')">返回订单</el-button>
      </div>
    </div>
    <div class="pay-card" v-else-if="error">
      <h2>支付异常</h2>
      <p>{{ error }}</p>
      <el-button @click="$router.push('/user')">返回订单</el-button>
    </div>
    <div class="pay-card" v-else>
      <p style="color:var(--text-muted)">正在生成支付...</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/api/request'

const route = useRoute()
const router = useRouter()
const payForm = ref('')
const orderNo = ref(route.query.orderNo as string || '')
const orderAmount = ref(route.query.amount as string || '0')
const orderId = ref(Number(route.query.orderId))
const error = ref('')
const checkingPayment = ref(false)

onMounted(async () => {
  if (!orderId.value) { error.value = '缺少订单ID'; return }
  try {
    const res = await request.post<any, any>('/pay/create', { orderId: orderId.value })
    const data = res.data ?? res
    payForm.value = data.payForm
    setTimeout(() => {
      const forms = document.querySelectorAll('.pay-form form')
      if (forms.length) (forms[0] as HTMLFormElement).submit()
    }, 100)
  } catch (e: any) {
    error.value = e?.response?.data?.message || '生成支付失败'
  }
})

async function checkPayment() {
  checkingPayment.value = true
  try {
    const qres = await request.post<any, any>('/pay/query', { orderId: orderId.value })
    const qstatus = (qres as any).data ?? qres
    if (String(qstatus) === 'PAID') {
      ElMessage.success('支付成功！')
      window.location.href = '/user'
      return
    }
    const cres = await request.post<any, any>('/pay/confirm', { orderId: orderId.value })
    const cstatus = (cres as any).data ?? cres
    if (String(cstatus) === 'PAID') {
      ElMessage.success('支付确认成功！')
      window.location.href = '/user'
    } else {
      ElMessage.warning('确认失败，请重试')
    }
  } catch (e: any) {
    ElMessage.error('操作失败: ' + (e?.response?.data?.message || e?.message || '未知错误'))
  }
  checkingPayment.value = false
}
</script>

<style scoped>
.pay-page { min-height: calc(100vh - 120px); display: flex; align-items: center; justify-content: center; background: var(--bg); padding: 24px; }
.pay-card { background: #fff; border-radius: 16px; padding: 48px; text-align: center; max-width: 500px; width: 100%; box-shadow: var(--shadow-lg); }
.pay-card h2 { font-size: 22px; margin-bottom: 20px; }
.pay-amount { font-size: 36px; font-weight: 700; color: var(--accent); margin-bottom: 8px; }
.pay-order-no { font-size: 13px; color: var(--text-muted); margin-bottom: 24px; }
.pay-form { margin: 0 auto; }
.pay-actions { margin-top: 20px; display: flex; flex-direction: column; gap: 10px; align-items: center; }
.pay-hint { font-size: 13px; color: var(--text-muted); }
</style>
