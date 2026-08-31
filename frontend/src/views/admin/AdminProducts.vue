<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { Product, Category } from '@/types'
import {
  getAdminProductsApi, createProductApi, updateProductApi,
  updateProductStatusApi, uploadImageApi,
} from '@/api/admin'
import { getCategoriesApi } from '@/api/category'
import { getSkusApi } from '@/api/sku'
import type { ProductSku } from '@/api/sku'
import request from '@/api/request'
import { formatPrice } from '@/utils/format'

const products = ref<Product[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const keyword = ref('')
const loading = ref(false)

const categories = ref<Category[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('添加商品')
const submitting = ref(false)
const isEdit = ref(false)
const editId = ref<number | null>(null)

const form = ref({
  name: '',
  description: '',
  categoryId: null as number | null,
  price: null as number | null,
  originalPrice: null as number | null,
  stock: null as number | null,
  badge: '',
  badgeText: '',
  image: '',
  images: '',
  isHot: 0,
  isNew: 0,
  sortOrder: 0,
  seckillPrice: null as number | null,
  seckillStock: null as number | null,
  seckillStart: '' as string,
  seckillEnd: '' as string,
})

const badgeOptions = [
  { label: '无', value: '' },
  { label: '新品 NEW', value: 'new' },
  { label: '热卖 HOT', value: 'hot' },
  { label: '折扣 SALE', value: 'sale' },
]

const uploading = ref(false)

// SKU Management
const skus = ref<ProductSku[]>([])
const skuForm = ref({ specs: '', stock: 0, price: '' as string })
const skuSaving = ref(false)

async function fetchProducts() {
  loading.value = true
  try {
    const res = await getAdminProductsApi({ page: page.value, size: size.value, keyword: keyword.value })
    products.value = res.records
    total.value = res.total
  } catch {
    ElMessage.error('加载商品失败')
  } finally {
    loading.value = false
  }
}

async function fetchCategories() {
  try {
    categories.value = await getCategoriesApi()
  } catch { /* fallback */ }
}

function handleSearch() {
  page.value = 1
  fetchProducts()
}

function handlePageChange(p: number) {
  page.value = p
  fetchProducts()
}

function openCreate() {
  isEdit.value = false
  editId.value = null
  dialogTitle.value = '添加商品'
  form.value = {
    name: '', description: '', categoryId: null, price: null,
    originalPrice: null, stock: null, badge: '', badgeText: '',
    image: '', images: '', isHot: 0, isNew: 0, sortOrder: 0,
    seckillPrice: null, seckillStock: null, seckillStart: '', seckillEnd: '',
  }
  skus.value = []
  dialogVisible.value = true
}

function openEdit(row: Product) {
  isEdit.value = true
  editId.value = row.id
  dialogTitle.value = '编辑商品'
  form.value = {
    name: row.name,
    description: row.description || '',
    categoryId: row.categoryId,
    price: row.price,
    originalPrice: row.originalPrice,
    stock: row.stock,
    badge: row.badge || '',
    badgeText: row.badgeText || '',
    image: row.image || '',
    images: row.images || '',
    isHot: row.isHot,
    isNew: row.isNew,
    sortOrder: row.sortOrder || 0,
    seckillPrice: row.seckillPrice,
    seckillStock: row.seckillStock,
    seckillStart: row.seckillStart || '',
    seckillEnd: row.seckillEnd || '',
  }
  dialogVisible.value = true
  // 加载该商品的 SKU
  getSkusApi(row.id).then(r => { skus.value = r }).catch(() => { skus.value = [] })
}

const fileInputRef = ref<HTMLInputElement | null>(null)

async function onFileChange(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  uploading.value = true
  try {
    const res = await uploadImageApi(file)
    form.value.image = (res as any).data ?? res
    ElMessage.success('上传成功')
  } catch {
    ElMessage.error('上传失败')
  } finally {
    uploading.value = false
    input.value = ''
  }
}

async function handleSubmit() {
  if (!form.value.name || !form.value.categoryId || !form.value.price || !form.value.stock) {
    ElMessage.warning('请填写必填项')
    return
  }
  submitting.value = true
  try {
    if (isEdit.value && editId.value) {
      await updateProductApi(editId.value, form.value)
      ElMessage.success('更新成功')
    } else {
      const res = await createProductApi(form.value) as any
      const newId = res?.data?.id || res?.id
      if (newId) await savePendingSkus(newId)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchProducts()
  } catch {
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

async function handleToggleStatus(row: Product) {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '上架' : '下架'
  try {
    await ElMessageBox.confirm(`确定要${action}该商品吗？`, '提示', { type: 'warning' })
    await updateProductStatusApi(row.id, newStatus)
    ElMessage.success(`${action}成功`)
    fetchProducts()
  } catch { /* cancelled */ }
}

// SKU Management (inline in product form)
async function addSkuInForm() {
  if (!skuForm.value.specs.trim()) { ElMessage.warning('请输入规格，如 黑色;M'); return }
  skuSaving.value = true
  try {
    if (isEdit.value && editId.value) {
      // 编辑模式：直接保存
      await request.post('/skus', {
        productId: editId.value,
        specs: skuForm.value.specs,
        stock: skuForm.value.stock,
        price: skuForm.value.price || null,
      })
      skus.value = await getSkusApi(editId.value)
    } else {
      // 新建模式：暂存本地，等商品创建后再批量保存
      skus.value.push({
        id: -(Date.now()), productId: 0, specs: skuForm.value.specs,
        stock: skuForm.value.stock, price: skuForm.value.price ? Number(skuForm.value.price) : null,
        skuCode: '', status: 1,
      } as any)
    }
    ElMessage.success('规格已添加')
    skuForm.value = { specs: '', stock: 0, price: '' }
  } catch { ElMessage.error('添加失败') }
  skuSaving.value = false
}

async function deleteSkuInForm(id: number) {
  if (id < 0) { skus.value = skus.value.filter(s => s.id !== id); return } // 暂存记录直接移除
  try { await request.delete('/skus/' + id); skus.value = skus.value.filter(s => s.id !== id) } catch { /* */ }
}

// 新建商品成功后，批量保存暂存的 SKU
async function savePendingSkus(productId: number) {
  for (const s of skus.value.filter(s => s.id < 0)) {
    await request.post('/skus', { productId, specs: s.specs, stock: s.stock, price: s.price || null })
  }
}

onMounted(() => {
  fetchProducts()
  fetchCategories()
})
</script>

<template>
  <div class="admin-products">
    <div class="page-header">
      <h2>商品管理</h2>
      <el-button type="primary" @click="openCreate">添加商品</el-button>
    </div>

    <div class="search-bar">
      <el-input v-model="keyword" placeholder="搜索商品名称" clearable @keyup.enter="handleSearch" style="width: 280px" />
      <el-button @click="handleSearch">搜索</el-button>
    </div>

    <el-table :data="products" v-loading="loading" style="width: 100%" border>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="商品名称" min-width="160" />
      <el-table-column label="图片" width="80">
        <template #default="{ row }">
          <img v-if="row.image" :src="row.image" style="width: 48px; height: 48px; object-fit: cover; border-radius: 4px" />
          <span v-else style="color: #ccc">无</span>
        </template>
      </el-table-column>
      <el-table-column label="价格" width="100" align="right">
        <template #default="{ row }">{{ formatPrice(row.price) }}</template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="80" align="center" />
      <el-table-column label="状态" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '上架' : '下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="热卖/新品" width="100" align="center">
        <template #default="{ row }">
          <span v-if="row.isHot" style="color: #e6a23c; margin-right: 6px">热卖</span>
          <span v-if="row.isNew" style="color: #67c23a">新品</span>
          <span v-if="!row.isHot && !row.isNew">-</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="openEdit(row)">编辑</el-button>
          <el-button
            :type="row.status === 1 ? 'warning' : 'success'"
            link
            size="small"
            @click="handleToggleStatus(row)"
          >
            {{ row.status === 1 ? '下架' : '上架' }}
          </el-button>
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

    <!-- Create/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px" top="20px" destroy-on-close>
      <el-form :model="form" label-width="90px" label-position="left">
        <el-row :gutter="20">
          <el-col :span="14">
            <el-form-item label="商品名称" required>
              <el-input v-model="form.name" placeholder="请输入商品名称" />
            </el-form-item>
          </el-col>
          <el-col :span="10">
            <el-form-item label="分类" required>
              <el-select v-model="form.categoryId" placeholder="选择分类" style="width:100%">
                <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="商品描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入商品描述" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="售价" required>
              <el-input-number v-model="form.price" :min="0" :precision="2" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="原价">
              <el-input-number v-model="form.originalPrice" :min="0" :precision="2" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="库存" required>
              <el-input-number v-model="form.stock" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="徽章">
              <el-select v-model="form.badge" placeholder="选择" style="width:100%">
                <el-option v-for="b in badgeOptions" :key="b.value" :label="b.label" :value="b.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="徽章文字">
              <el-input v-model="form.badgeText" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="排序">
              <el-input-number v-model="form.sortOrder" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="热卖">
              <el-switch v-model="form.isHot" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="新品">
              <el-switch v-model="form.isNew" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="秒杀库存">
              <el-input-number v-model="form.seckillStock" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="秒杀价">
              <el-input-number v-model="form.seckillPrice" :min="0" :precision="2" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="秒杀开始时间">
              <el-date-picker
                v-model="form.seckillStart"
                type="datetime"
                placeholder="选择开始时间"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width:100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="秒杀结束时间">
              <el-date-picker
                v-model="form.seckillEnd"
                type="datetime"
                placeholder="选择结束时间"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width:100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- SKU 规格管理 -->
        <el-divider content-position="left">规格管理 (SKU)</el-divider>
        <div class="sku-list-inline">
          <div v-if="!skus.length" class="sku-empty-inline">暂无规格，请在下方添加</div>
          <el-tag
            v-for="s in skus" :key="s.id" closable
            style="margin:4px"
            @close="deleteSkuInForm(s.id)"
          >
            {{ s.specs }} | 库存{{ s.stock }}{{ s.price ? ' | ¥'+s.price : '' }}
          </el-tag>
        </div>
        <div class="sku-add-inline">
          <el-input v-model="skuForm.specs" placeholder="规格如: 黑色;M" size="small" style="width:120px" />
          <el-input-number v-model="skuForm.stock" :min="0" size="small" style="width:90px" placeholder="库存" />
          <el-input v-model="skuForm.price" placeholder="价格(可选)" size="small" style="width:100px" />
          <el-button type="primary" size="small" :loading="skuSaving" @click="addSkuInForm">添加规格</el-button>
        </div>

        <el-form-item label="商品主图">
          <div class="upload-area">
            <input ref="fileInputRef" type="file" accept="image/*" style="display: none" @change="onFileChange" />
            <div v-if="!form.image" class="upload-placeholder" @click="fileInputRef?.click()">
              <span>{{ uploading ? '上传中...' : '点击上传' }}</span>
            </div>
            <img v-else :src="form.image" class="upload-preview" @click="fileInputRef?.click()" :title="form.image" />
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          {{ isEdit ? '保存' : '添加' }}
        </el-button>
      </template>
    </el-dialog>

  </div>
</template>

<style scoped>
.sku-list-inline { margin-bottom: 8px; min-height: 32px; }
.sku-empty-inline { color: var(--text-muted); font-size: 13px; padding: 4px 0; }
.sku-add-inline { display: flex; gap: 8px; align-items: center; }
.admin-products { max-width: 1200px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { font-size: 22px; font-weight: 700; color: var(--primary); margin: 0; }
.search-bar { display: flex; gap: 10px; margin-bottom: 16px; }
.pagination-wrap { display: flex; justify-content: center; margin-top: 24px; }
.upload-area { display: flex; align-items: center; }
.upload-placeholder {
  width: 120px; height: 120px; border: 2px dashed var(--border);
  border-radius: 8px; display: flex; align-items: center; justify-content: center;
  cursor: pointer; color: var(--text-muted); font-size: 13px;
}
.upload-placeholder:hover { border-color: var(--accent); color: var(--accent); }
.upload-preview { width: 120px; height: 120px; object-fit: cover; border-radius: 8px; cursor: pointer; }
</style>
