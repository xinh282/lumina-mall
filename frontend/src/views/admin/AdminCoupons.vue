<template>
  <div class="admin-coupons">
    <div class="page-header">
      <h2>优惠券管理</h2>
      <el-button type="primary" @click="showCreateDialog = true">新建优惠券</el-button>
    </div>

    <el-table :data="records" stripe v-loading="loading" style="width: 100%">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="name" label="优惠券名称" min-width="140" />
      <el-table-column prop="type" label="类型" width="100">
        <template #default="{ row }">{{ row.type === 'FIXED' ? '满减券' : '折扣券' }}</template>
      </el-table-column>
      <el-table-column label="优惠" width="120">
        <template #default="{ row }">
          <template v-if="row.type === 'FIXED'">满{{ row.threshold }}减{{ row.discountValue }}</template>
          <template v-else>满{{ row.threshold }}打{{ row.discountValue }}折</template>
        </template>
      </el-table-column>
      <el-table-column label="使用情况" width="120">
        <template #default="{ row }">{{ row.usedCount }} / {{ row.totalCount }}</template>
      </el-table-column>
      <el-table-column prop="expireDays" label="有效天数" width="90" />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
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

    <!-- Create Dialog -->
    <el-dialog v-model="showCreateDialog" title="新建优惠券" width="480px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="如：新用户专享券" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio value="FIXED">满减券</el-radio>
            <el-radio value="PERCENT">折扣券</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="使用门槛" prop="threshold">
          <el-input-number v-model="form.threshold" :min="0" :precision="2" style="width: 200px" />
          <span class="form-hint">元</span>
        </el-form-item>
        <el-form-item label="优惠值" prop="discountValue">
          <el-input-number
            v-model="form.discountValue"
            :min="0"
            :precision="form.type === 'PERCENT' ? 0 : 2"
            :max="form.type === 'PERCENT' ? 10 : 999999"
            style="width: 200px"
          />
          <span class="form-hint">{{ form.type === 'PERCENT' ? '折（1-10）' : '元' }}</span>
        </el-form-item>
        <el-form-item label="发放数量" prop="totalCount">
          <el-input-number v-model="form.totalCount" :min="1" style="width: 200px" />
        </el-form-item>
        <el-form-item label="有效天数" prop="expireDays">
          <el-input-number v-model="form.expireDays" :min="1" style="width: 200px" />
          <span class="form-hint">天</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="handleCreate">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAdminCouponsApi, createCouponApi } from '@/api/admin'
import type { Coupon } from '@/api/admin'
import type { FormInstance, FormRules } from 'element-plus'

const records = ref<Coupon[]>([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)

onMounted(() => fetchList())

async function fetchList() {
  loading.value = true
  try {
    const res = await getAdminCouponsApi({ page: page.value, size: size.value })
    records.value = res.records
    total.value = res.total
  } catch { /* ignore */ }
  loading.value = false
}

// Create
const showCreateDialog = ref(false)
const creating = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<Coupon>({
  id: 0,
  name: '',
  type: 'FIXED',
  threshold: 0,
  discountValue: 0,
  totalCount: 1,
  usedCount: 0,
  expireDays: 7,
  status: 1,
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入优惠券名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  threshold: [{ required: true, message: '请输入使用门槛', trigger: 'blur' }],
  discountValue: [{ required: true, message: '请输入优惠值', trigger: 'blur' }],
  totalCount: [{ required: true, message: '请输入发放数量', trigger: 'blur' }],
  expireDays: [{ required: true, message: '请输入有效天数', trigger: 'blur' }],
}

function resetForm() {
  formRef.value?.resetFields()
  form.name = ''
  form.type = 'FIXED'
  form.threshold = 0
  form.discountValue = 0
  form.totalCount = 1
  form.expireDays = 7
}

async function handleCreate() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  creating.value = true
  try {
    await createCouponApi(form)
    ElMessage.success('优惠券创建成功')
    showCreateDialog.value = false
    fetchList()
  } catch { /* ignore */ }
  creating.value = false
}
</script>

<style scoped>
.admin-coupons {
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

.form-hint {
  margin-left: 8px;
  font-size: 13px;
  color: var(--text-muted);
}
</style>
