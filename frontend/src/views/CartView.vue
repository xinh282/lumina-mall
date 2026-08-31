<template>
  <div class="cart-page">
    <div class="cart-container">
      <h1 class="page-title">我的购物车</h1>

      <template v-if="extendedItems.length">
        <!-- Select All -->
        <div class="select-all-row">
          <el-checkbox
            v-model="selectAll"
            :indeterminate="isIndeterminate"
            @change="toggleSelectAll"
          >
            全选
          </el-checkbox>
        </div>

        <!-- Cart Items -->
        <el-card
          v-for="item in extendedItems"
          :key="item.id"
          class="cart-item-card"
          shadow="none"
        >
          <div class="cart-item-row">
            <el-checkbox v-model="item.selected" class="item-checkbox" />

            <div class="item-image">
              <div class="img-placeholder">
                <img
                  v-if="item.productImage"
                  :src="item.productImage"
                  :alt="item.productName"
                />
                <span v-else>商品图</span>
              </div>
            </div>

            <div class="item-info">
              <p class="item-name">{{ item.productName }}</p>
              <p v-if="item.specs" class="item-specs">{{ item.specs }}</p>
              <p class="item-unit-price">{{ formatPrice(item.productPrice) }}</p>
            </div>

            <div class="item-quantity">
              <div class="quantity-selector">
                <button
                  class="qty-btn"
                  :disabled="item.quantity <= 1"
                  @click="handleUpdateQuantity(item, item.quantity - 1)"
                >
                  -
                </button>
                <span class="qty-value">{{ item.quantity }}</span>
                <button
                  class="qty-btn"
                  @click="handleUpdateQuantity(item, item.quantity + 1)"
                >
                  +
                </button>
              </div>
            </div>

            <div class="item-subtotal">
              <span class="subtotal-price">
                {{ formatPrice(item.productPrice * item.quantity) }}
              </span>
            </div>

            <el-button
              type="danger"
              link
              class="item-delete"
              @click="handleRemoveItem(item)"
            >
              删除
            </el-button>
          </div>
        </el-card>

        <!-- Bottom Summary Bar -->
        <div class="bottom-bar">
          <div class="bottom-left">
            <el-checkbox
              v-model="selectAll"
              :indeterminate="isIndeterminate"
              @change="toggleSelectAll"
            >
              全选
            </el-checkbox>
          </div>
          <div class="bottom-right">
            <span class="selected-info">
              已选 <strong>{{ selectedCount }}</strong> 件
            </span>
            <span class="total-price-label">合计：</span>
            <span class="total-price">{{ formatPrice(selectedTotalPrice) }}</span>
            <el-button
              type="primary"
              size="large"
              class="checkout-btn"
              :disabled="selectedCount === 0"
              @click="showAddressDialog = true"
            >
              去结算
            </el-button>
          </div>
        </div>
      </template>

      <EmptyState v-else title="购物车是空的" />
    </div>

    <!-- Address Dialog -->
    <el-dialog v-model="showAddressDialog" title="收货地址" width="480px" :close-on-click-modal="false">
      <div v-if="savedAddresses.length" class="saved-addrs">
        <div v-for="a in savedAddresses" :key="a.id" class="saved-addr-row"
             :class="{ picked: addressForm.receiverName === a.receiverName }"
             @click="addressForm.receiverName = a.receiverName; addressForm.receiverPhone = a.receiverPhone; addressForm.receiverAddress = a.receiverAddress">
          <strong>{{ a.receiverName }}</strong> {{ a.receiverPhone }}
          <div class="saved-addr-text">{{ a.receiverAddress }}</div>
        </div>
      </div>
      <el-form
        ref="addressFormRef"
        :model="addressForm"
        :rules="addressRules"
        label-width="80px"
      >
        <el-form-item label="收货人" prop="receiverName">
          <el-input v-model="addressForm.receiverName" placeholder="请输入收货人姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="receiverPhone">
          <el-input v-model="addressForm.receiverPhone" placeholder="请输入手机号码" />
        </el-form-item>
        <el-form-item label="地址" prop="receiverAddress">
          <el-input
            v-model="addressForm.receiverAddress"
            type="textarea"
            :rows="3"
            placeholder="请输入详细地址"
          />
        </el-form-item>
        <el-form-item label="优惠券">
          <el-select
            v-model="selectedCouponId"
            placeholder="选择优惠券（可选）"
            clearable
            style="width: 100%"
            :loading="discountLoading"
            @change="onCouponChange"
          >
            <el-option
              v-for="c in coupons"
              :key="c.id"
              :label="`${c.name}（${c.type === 'FIXED' ? '满' + c.threshold + '减' + c.discountValue : '满' + c.threshold + '打' + c.discountValue + '折'}）${selectedTotalPrice < c.threshold ? ' — 未达门槛' : ''}`"
              :value="c.id"
              :disabled="selectedTotalPrice < c.threshold"
            />
          </el-select>
          <p v-if="coupons.length === 0 && !discountLoading" class="coupon-discount-notice" style="color: var(--text-muted)">暂无可用优惠券</p>
          <p v-if="couponDiscount > 0" class="coupon-discount-notice">
            已优惠 <strong>¥{{ couponDiscount }}</strong>，实付 <strong>¥{{ finalAmount }}</strong>
          </p>
        </el-form-item>
        <el-form-item label="设为默认">
          <el-switch v-model="addressForm.saveAsDefault" :active-value="1" :inactive-value="0" />
          <span class="default-tip">下次下单自动带入</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddressDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmitOrder">
          确认下单
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cart'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatPrice } from '@/utils/format'
import EmptyState from '@/components/common/EmptyState.vue'
import { createOrderApi } from '@/api/order'
import { getMyCouponsApi, calcDiscountApi } from '@/api/coupon'
import type { UserCoupon } from '@/api/coupon'
import { getDefaultAddressApi, saveAddressApi } from '@/api/address'
import request from '@/api/request'
import type { CartItem, Address } from '@/types'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const cartStore = useCartStore()

// Extend CartItem with runtime selected state
interface ExtendedCartItem extends CartItem {
  selected: boolean
}

const selectAll = ref(true)

const extendedItems = computed<ExtendedCartItem[]>(() =>
  (cartStore.items ?? []).map((item: CartItem) => ({
    ...item,
    selected: true,
  }))
)

const isIndeterminate = computed(() => {
  const items = extendedItems.value
  const sel = items.filter((i) => i.selected).length
  return sel > 0 && sel < items.length
})

const selectedCount = computed(
  () => extendedItems.value.filter((i) => i.selected).length
)

const selectedTotalPrice = computed(() =>
  extendedItems.value
    .filter((i) => i.selected)
    .reduce((sum, i) => sum + i.productPrice * i.quantity, 0)
)

function toggleSelectAll(val: boolean) {
  extendedItems.value.forEach((i) => {
    i.selected = val
  })
  selectAll.value = val
}

async function handleUpdateQuantity(item: ExtendedCartItem, qty: number) {
  if (qty < 1) return
  try {
    await cartStore.updateQuantity(item.id, qty)
  } catch {
    ElMessage.error('更新数量失败')
  }
}

async function handleRemoveItem(item: ExtendedCartItem) {
  try {
    await ElMessageBox.confirm('确定要删除该商品吗？', '提示', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await cartStore.removeItem(item.id)
    ElMessage.success('已删除')
  } catch {
    // user cancelled
  }
}

// Address dialog
const showAddressDialog = ref(false)
const submitting = ref(false)
const addressFormRef = ref<FormInstance>()
const addressForm = reactive({
  receiverName: '',
  receiverPhone: '',
  receiverAddress: '',
  saveAsDefault: 1,
})

// Coupon
const coupons = ref<UserCoupon[]>([])
const selectedCouponId = ref<number | undefined>()
const couponDiscount = ref(0)
const discountLoading = ref(false)

const addressRules: FormRules = {
  receiverName: [
    { required: true, message: '请输入收货人姓名', trigger: 'blur' },
  ],
  receiverPhone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' },
  ],
  receiverAddress: [
    { required: true, message: '请输入详细地址', trigger: 'blur' },
    { min: 5, message: '地址至少5个字符', trigger: 'blur' },
  ],
}

async function handleSubmitOrder() {
  const valid = await addressFormRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const selectedItems = extendedItems.value.filter((i) => i.selected)
    const cartItemIds = selectedItems.map((i) => i.id)

    const res = await createOrderApi({
      receiverName: addressForm.receiverName,
      receiverPhone: addressForm.receiverPhone,
      receiverAddress: addressForm.receiverAddress,
      cartItemIds,
      userCouponId: selectedCouponId.value,
    })

    // 保存地址
    try {
      await saveAddressApi({
        receiverName: addressForm.receiverName,
        receiverPhone: addressForm.receiverPhone,
        receiverAddress: addressForm.receiverAddress,
        saveAsDefault: addressForm.saveAsDefault,
      })
    } catch { /* 地址保存失败不影响下单 */ }

    ElMessage.success('下单成功！')
    showAddressDialog.value = false

    await cartStore.fetchCart()
    router.push('/pay?orderId=' + res.id + '&orderNo=' + res.orderNo + '&amount=' + selectedTotalPrice.value)
  } catch {
    ElMessage.error('下单失败，请重试')
  } finally {
    submitting.value = false
  }
}

async function loadDefaultAddress() {
  try {
    const addr = await getDefaultAddressApi()
    if (addr) {
      addressForm.receiverName = addr.receiverName
      addressForm.receiverPhone = addr.receiverPhone
      addressForm.receiverAddress = addr.receiverAddress
    }
  } catch { /* ignore */ }
}

async function fetchCoupons() {
  try {
    coupons.value = await getMyCouponsApi()
  } catch { /* ignore */ }
}

async function onCouponChange(id: number | undefined) {
  if (!id) {
    couponDiscount.value = 0
    return
  }
  discountLoading.value = true
  try {
    const res = await calcDiscountApi(id, selectedTotalPrice.value)
    couponDiscount.value = res.discount
  } catch {
    couponDiscount.value = 0
  }
  discountLoading.value = false
}

const finalAmount = computed(() => selectedTotalPrice.value - couponDiscount.value)

const savedAddresses = ref<Address[]>([])

watch(showAddressDialog, async (open) => {
  if (open) {
    loadDefaultAddress()
    fetchCoupons()
    selectedCouponId.value = undefined
    couponDiscount.value = 0
    try { const res = await request.get<any, Address[]>('/address'); savedAddresses.value = res.data ?? res } catch {}
  }
})

onMounted(() => {
  cartStore.fetchCart()
})
</script>

<style scoped>
.cart-page {
  background: var(--bg);
  min-height: calc(100vh - 120px);
}

.cart-container {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 48px 24px;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--text);
  margin: 0 0 32px;
  letter-spacing: 1px;
}

.select-all-row {
  margin-bottom: 16px;
  padding: 0 8px;
}

.cart-item-card {
  margin-bottom: 12px;
  border: 1px solid var(--border);
  border-radius: var(--radius, 10px);
}

.cart-item-card :deep(.el-card__body) {
  padding: 16px 20px;
}

.cart-item-row {
  display: flex;
  align-items: center;
  gap: 16px;
}

.item-checkbox {
  flex-shrink: 0;
}

.item-image {
  flex-shrink: 0;
  width: 72px;
  height: 72px;
}

.img-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #f5f3ef, #e8e4df);
  border-radius: var(--radius-sm, 6px);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: var(--text-muted);
  overflow: hidden;
}

.img-placeholder img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-info {
  flex: 1;
  min-width: 0;
}

.item-name {
  font-size: 15px;
  color: var(--text);
  margin: 0 0 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-unit-price {
  font-size: 13px;
  color: var(--text-muted);
  margin: 0;
}
.item-specs {
  font-size: 12px;
  color: var(--text-muted);
  margin: 0 0 2px;
}

.item-quantity {
  flex-shrink: 0;
}

.quantity-selector {
  display: flex;
  align-items: center;
  border: 1px solid var(--border);
  border-radius: var(--radius-sm, 6px);
  overflow: hidden;
}

.qty-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: var(--bg);
  color: var(--text);
  font-size: 14px;
  cursor: pointer;
  transition: background var(--transition);
}

.qty-btn:hover:not(:disabled) {
  background: #e8e4df;
}

.qty-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.qty-value {
  width: 40px;
  text-align: center;
  font-size: 14px;
  color: var(--text);
  border-left: 1px solid var(--border);
  border-right: 1px solid var(--border);
  line-height: 32px;
}

.item-subtotal {
  flex-shrink: 0;
  min-width: 80px;
  text-align: right;
}

.subtotal-price {
  font-size: 17px;
  font-weight: 700;
  color: var(--text);
}

.item-delete {
  flex-shrink: 0;
}

/* Bottom Bar */
.bottom-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 24px;
  padding: 20px 24px;
  background: #fff;
  border: 1px solid var(--border);
  border-radius: var(--radius, 10px);
  box-shadow: var(--shadow-md);
  position: sticky;
  bottom: 24px;
}

.bottom-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.selected-info {
  font-size: 14px;
  color: var(--text-muted);
}

.selected-info strong {
  color: var(--accent);
}

.total-price-label {
  font-size: 14px;
  color: var(--text-muted);
}

.total-price {
  font-size: 22px;
  font-weight: 700;
  color: var(--accent);
  font-family: var(--font-serif, serif);
}

.checkout-btn {
  height: 46px;
  font-size: 15px;
  background-color: var(--primary);
  border-color: var(--primary);
  padding: 0 36px;
  border-radius: var(--radius, 10px);
  letter-spacing: 1px;
}

.default-tip {
  margin-left: 8px;
  font-size: 12px;
  color: var(--text-muted);
}

.coupon-discount-notice {
  margin: 6px 0 0;
  font-size: 13px;
  color: var(--accent);
}

.saved-addrs { margin-bottom: 16px; }
.saved-addr-row { padding: 10px; border: 1px solid var(--border); border-radius: 6px; cursor: pointer; margin-bottom: 6px; font-size: 13px; transition: all .2s; }
.saved-addr-row:hover { border-color: var(--accent); }
.saved-addr-row.picked { border-color: var(--accent); background: var(--accent-light); }
.saved-addr-text { color: var(--text-muted); font-size: 12px; margin-top: 2px; }

.coupon-discount-notice strong {
  font-weight: 700;
}

.checkout-btn:hover {
  opacity: 0.9;
}

@media (max-width: 768px) {
  .cart-container {
    padding: 32px 16px;
  }

  .page-title {
    font-size: 22px;
  }

  .cart-item-row {
    flex-wrap: wrap;
    gap: 12px;
  }

  .item-subtotal {
    margin-left: auto;
  }

  .bottom-bar {
    flex-direction: column;
    gap: 16px;
    padding: 16px;
  }

  .bottom-right {
    flex-wrap: wrap;
    gap: 8px;
    width: 100%;
  }

  .checkout-btn {
    width: 100%;
  }
}
</style>
