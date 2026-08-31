<template>
  <div class="user-center">
    <div class="uc-container">
      <h1>个人中心</h1>
      <div class="uc-layout">
        <div class="uc-sidebar">
          <div class="uc-avatar">
          <img v-if="userStore.userInfo?.avatar" :src="userStore.userInfo.avatar" class="uc-avatar-img" />
          <span v-else>{{ (userStore.userInfo?.nickname || userStore.userInfo?.username || 'U').charAt(0).toUpperCase() }}</span>
        </div>
          <div class="uc-username">{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</div>
          <div class="uc-menu">
            <button v-for="t in tabs" :key="t.key" class="uc-menu-item" :class="{ active: activeTab === t.key }" @click="activeTab = t.key">
              <span>{{ t.icon }}</span> {{ t.label }}
            </button>
          </div>
        </div>
        <div class="uc-content">
          <div v-if="activeTab === 'profile'" class="tab-panel"><UserProfile /></div>
          <div v-if="activeTab === 'orders'" class="tab-panel"><OrderList /></div>
          <div v-if="activeTab === 'refunds'" class="tab-panel"><RefundList /></div>
          <div v-if="activeTab === 'favorites'" class="tab-panel"><FavoriteList /></div>
          <div v-if="activeTab === 'addresses'" class="tab-panel"><AddressList /></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import UserProfile from '@/components/user/UserProfile.vue'
import OrderList from '@/components/user/OrderList.vue'
import RefundList from '@/components/user/RefundList.vue'
import FavoriteList from '@/components/user/FavoriteList.vue'
import AddressList from '@/components/user/AddressList.vue'

const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()
const activeTab = ref('orders')

const tabs = [
  { key: 'orders', label: '我的订单', icon: '📦' },
  { key: 'favorites', label: '我的收藏', icon: '❤️' },
  { key: 'addresses', label: '收货地址', icon: '📍' },
  { key: 'refunds', label: '退款记录', icon: '💰' },
  { key: 'profile', label: '个人信息', icon: '👤' },
]

onMounted(() => {
  if (!userStore.isLoggedIn) { router.push('/login?redirect=/user'); return }
  cartStore.fetchCart()
})
</script>

<style scoped>
.user-center { min-height: calc(100vh - 120px); background: var(--bg); }
.uc-container { max-width: 960px; margin: 0 auto; padding: 40px 24px; }
.uc-container h1 { font-family: var(--font-display); font-size: 26px; font-weight: 700; margin: 0 0 32px; }

.uc-layout { display: flex; gap: 32px; }
.uc-sidebar { width: 180px; flex-shrink: 0; text-align: center; }
.uc-avatar {
  width: 64px; height: 64px; border-radius: 50%; background: linear-gradient(135deg, var(--accent), #c0392b);
  color: #fff; font-size: 26px; font-weight: 700; display: flex; align-items: center; justify-content: center;
  margin: 0 auto 12px; overflow: hidden;
}
.uc-avatar-img { width: 100%; height: 100%; object-fit: cover; }
.uc-username { font-size: 15px; font-weight: 600; margin-bottom: 24px; }
.uc-menu { display: flex; flex-direction: column; gap: 4px; }
.uc-menu-item {
  display: flex; align-items: center; gap: 8px; padding: 10px 14px; border-radius: 8px;
  font-size: 14px; color: var(--text-secondary); cursor: pointer; transition: all .15s;
  text-align: left; width: 100%; border: none; background: none;
}
.uc-menu-item:hover { background: var(--bg-warm); color: var(--text); }
.uc-menu-item.active { background: var(--text); color: #fff; font-weight: 600; }

.uc-content { flex: 1; min-width: 0; }
.tab-panel { animation: fadeIn .2s ease; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(6px); } to { opacity: 1; transform: translateY(0); } }

@media (max-width: 768px) {
  .uc-layout { flex-direction: column; }
  .uc-sidebar { width: 100%; display: flex; align-items: center; gap: 12px; text-align: left; }
  .uc-avatar { width: 40px; height: 40px; font-size: 18px; margin: 0; }
  .uc-username { margin-bottom: 0; flex: 1; }
  .uc-menu { flex-direction: row; flex-wrap: wrap; }
  .uc-menu-item { font-size: 12px; padding: 6px 10px; }
}
</style>
