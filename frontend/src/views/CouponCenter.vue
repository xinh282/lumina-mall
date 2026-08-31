<template>
  <div class="coupon-center">
    <div class="container">
      <h1>领券中心</h1>

      <div class="section">
        <h2 class="section-title">可领取优惠券</h2>
        <div v-if="availableCoupons.length" class="coupon-grid">
          <div v-for="c in availableCoupons" :key="c.id" class="coupon-card" :class="{ claimed: claimedIds.has(c.id) }">
            <div class="coupon-left">
              <div class="coupon-value">
                <template v-if="c.type === 'FIXED'">
                  <span class="symbol">¥</span>{{ c.discountValue }}
                </template>
                <template v-else>
                  {{ c.discountValue }}<span class="symbol">折</span>
                </template>
              </div>
              <div class="coupon-condition">满{{ c.threshold }}可用</div>
            </div>
            <div class="coupon-right">
              <div class="coupon-name">{{ c.name }}</div>
              <div class="coupon-meta">有效期 {{ c.expireDays }} 天 · 剩余 {{ c.totalCount - c.usedCount }} 张</div>
              <el-button
                type="primary"
                size="small"
                :disabled="claimedIds.has(c.id)"
                :loading="claimingId === c.id"
                @click="handleClaim(c.id)"
              >
                {{ claimedIds.has(c.id) ? '已领取' : '立即领取' }}
              </el-button>
            </div>
          </div>
        </div>
        <div v-else class="empty">暂无可领取的优惠券</div>
      </div>

      <div class="section">
        <h2 class="section-title">我的优惠券</h2>
        <div v-if="myCoupons.length" class="coupon-grid">
          <div v-for="c in myCoupons" :key="c.id" class="coupon-card used">
            <div class="coupon-left">
              <div class="coupon-value">
                <template v-if="c.type === 'FIXED'">
                  <span class="symbol">¥</span>{{ c.discountValue }}
                </template>
                <template v-else>
                  {{ c.discountValue }}<span class="symbol">折</span>
                </template>
              </div>
              <div class="coupon-condition">满{{ c.threshold }}可用</div>
            </div>
            <div class="coupon-right">
              <div class="coupon-name">{{ c.name }}</div>
              <div class="coupon-meta">有效期 {{ c.expireDays }} 天</div>
              <el-tag size="small" type="success">已领取</el-tag>
            </div>
          </div>
        </div>
        <div v-else class="empty">暂无优惠券，快去领取吧</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAvailableCouponsApi, claimCouponApi, getMyCouponsApi } from '@/api/coupon'
import type { AvailableCoupon, UserCoupon } from '@/api/coupon'

const availableCoupons = ref<AvailableCoupon[]>([])
const myCoupons = ref<UserCoupon[]>([])
const claimedIds = ref(new Set<number>())
const claimingId = ref(0)

onMounted(async () => {
  try {
    const [available, mine] = await Promise.all([
      getAvailableCouponsApi(),
      getMyCouponsApi(),
    ])
    availableCoupons.value = available
    myCoupons.value = mine
    // 标记已领取的（我的优惠券对应 available 中的 couponId 关系，这里简化：名称匹配）
    const myNames = new Set(mine.map(m => m.name))
    available.forEach(c => {
      if (myNames.has(c.name)) claimedIds.value.add(c.id)
    })
  } catch { /* ignore */ }
})

async function handleClaim(couponId: number) {
  claimingId.value = couponId
  try {
    await claimCouponApi(couponId)
    claimedIds.value.add(couponId)
    ElMessage.success('领取成功')
    // 刷新我的优惠券
    myCoupons.value = await getMyCouponsApi()
  } catch {
    ElMessage.error('领取失败')
  } finally {
    claimingId.value = 0
  }
}
</script>

<style scoped>
.coupon-center {
  min-height: calc(100vh - 120px);
  background: var(--bg);
}

.container {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 48px 24px;
}

h1 {
  font-size: 28px;
  font-weight: 700;
  color: var(--text);
  margin: 0 0 40px;
  letter-spacing: 1px;
}

.section {
  margin-bottom: 48px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text);
  margin: 0 0 20px;
  padding-bottom: 12px;
  border-bottom: 2px solid var(--border);
}

.coupon-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 16px;
}

.coupon-card {
  display: flex;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid var(--border);
  background: #fff;
  transition: transform 0.2s, box-shadow 0.2s;
}

.coupon-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0,0,0,0.08);
}

.coupon-card.claimed {
  opacity: 0.6;
}

.coupon-left {
  width: 130px;
  min-width: 130px;
  background: linear-gradient(135deg, var(--accent), #c0392b);
  color: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px 12px;
}

.coupon-card.used .coupon-left {
  background: linear-gradient(135deg, #999, #777);
}

.coupon-value {
  font-size: 32px;
  font-weight: 800;
  line-height: 1;
}

.symbol {
  font-size: 16px;
  font-weight: 600;
}

.coupon-condition {
  font-size: 12px;
  margin-top: 6px;
  opacity: 0.85;
}

.coupon-right {
  flex: 1;
  padding: 20px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 8px;
}

.coupon-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--text);
}

.coupon-meta {
  font-size: 12px;
  color: var(--text-muted);
}

.empty {
  text-align: center;
  padding: 48px 24px;
  color: var(--text-muted);
  font-size: 15px;
  background: #fff;
  border-radius: 12px;
  border: 1px solid var(--border);
}

@media (max-width: 768px) {
  .container {
    padding: 32px 16px;
  }
  .coupon-grid {
    grid-template-columns: 1fr;
  }
}
</style>
