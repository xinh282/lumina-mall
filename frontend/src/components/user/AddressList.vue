<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/request'
import type { Address } from '@/types'

const addresses = ref<Address[]>([])
const loading = ref(false)

onMounted(() => fetchAddresses())

async function fetchAddresses() {
  loading.value = true
  try { const res = await request.get<any, Address[]>('/address'); addresses.value = res.data ?? res } catch {}
  loading.value = false
}

async function deleteAddr(id: number) {
  try {
    await ElMessageBox.confirm('删除该地址？', '提示', { type: 'warning' })
    await request.delete('/address/' + id)
    ElMessage.success('已删除')
    fetchAddresses()
  } catch {}
}

const showForm = ref(false)
const form = ref({ receiverName: '', receiverPhone: '', receiverAddress: '' })
const saving = ref(false)

async function saveAddr() {
  if (!form.value.receiverName || !form.value.receiverPhone || !form.value.receiverAddress) {
    ElMessage.warning('请填写完整')
    return
  }
  saving.value = true
  try {
    await request.post('/address', { ...form.value, isDefault: 1 })
    ElMessage.success('已保存')
    showForm.value = false
    form.value = { receiverName: '', receiverPhone: '', receiverAddress: '' }
    fetchAddresses()
  } catch {}
  saving.value = false
}
</script>

<template>
  <div class="addr-list" v-loading="loading">
    <div class="addr-actions"><el-button size="small" @click="showForm = !showForm">{{ showForm ? '取消' : '新增地址' }}</el-button></div>
    <div v-if="showForm" class="addr-form">
      <el-input v-model="form.receiverName" placeholder="收货人" size="small" style="margin-bottom:8px" />
      <el-input v-model="form.receiverPhone" placeholder="手机号" size="small" style="margin-bottom:8px" />
      <el-input v-model="form.receiverAddress" placeholder="详细地址" size="small" type="textarea" :rows="2" style="margin-bottom:8px" />
      <el-button type="primary" size="small" :loading="saving" @click="saveAddr">保存</el-button>
    </div>

    <div v-if="!addresses.length && !loading" class="empty">暂无地址</div>
    <div v-for="a in addresses" :key="a.id" class="addr-row">
      <div class="addr-info">
        <strong>{{ a.receiverName }}</strong> {{ a.receiverPhone }}
        <div class="addr-text">{{ a.receiverAddress }}</div>
      </div>
      <el-tag v-if="a.isDefault" size="small" type="warning">默认</el-tag>
      <el-button type="danger" link size="small" @click="deleteAddr(a.id)">删除</el-button>
    </div>
  </div>
</template>

<style scoped>
.addr-actions { margin-bottom: 12px; }
.addr-form { padding: 12px; background: var(--bg-warm); border-radius: 8px; margin-bottom: 12px; }
.addr-row { display: flex; align-items: center; gap: 10px; padding: 10px 0; border-bottom: 1px solid var(--border-light); }
.addr-info { flex: 1; font-size: 13px; }
.addr-text { color: var(--text-muted); font-size: 12px; margin-top: 2px; }
.empty { text-align: center; padding: 48px; color: var(--text-muted); }
</style>
