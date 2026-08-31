<template>
  <header class="site-header" :class="{ scrolled: isScrolled }">
    <div class="header-inner">
      <router-link to="/" class="logo">LUMINA</router-link>

      <nav class="main-nav">
        <router-link to="/" class="nav-link" exact-active-class="active">首页</router-link>
        <router-link v-for="c in categories" :key="c.id" :to="'/category/' + c.id" class="nav-link" active-class="active">{{ c.name }}</router-link>
      </nav>

      <div class="header-right">
        <router-link v-if="userStore.isLoggedIn && !userStore.isAdmin" to="/coupons" class="nav-link">领券</router-link>
        <router-link v-if="userStore.isAdmin" to="/admin" class="nav-link accent">管理后台</router-link>

        <!-- Search -->
        <form class="search-form" @submit.prevent="onSearch">
          <svg class="search-icon" viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
          <input v-model="searchQuery" class="search-input" type="text" placeholder="搜索商品..." />
        </form>

        <button v-if="userStore.isLoggedIn" class="icon-btn" @click="goToNotifications">
          <svg viewBox="0 0 24 24" width="19" height="19" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg>
          <span v-if="unreadCount > 0" class="dot">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
        </button>

        <button class="icon-btn" @click="goToUser">
          <svg viewBox="0 0 24 24" width="19" height="19" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
        </button>

        <button class="icon-btn cart-btn" @click="$emit('open-cart')">
          <svg viewBox="0 0 24 24" width="19" height="19" fill="none" stroke="currentColor" stroke-width="1.8"><circle cx="9" cy="21" r="1"/><circle cx="20" cy="21" r="1"/><path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"/></svg>
          <span v-if="cartStore.totalCount > 0" class="dot">{{ cartStore.totalCount }}</span>
        </button>

        <button v-if="userStore.isLoggedIn" class="icon-btn" @click="handleLogout" title="退出">
          <svg viewBox="0 0 24 24" width="17" height="17" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>
        </button>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useCartStore } from '@/stores/cart'
import { useUserStore } from '@/stores/user'
import { getCategoriesApi } from '@/api/category'
import { getUnreadCountApi } from '@/api/notification'
import { ElMessage } from 'element-plus'
import type { Category } from '@/types'

const router = useRouter()
const route = useRoute()
const cartStore = useCartStore()
const userStore = useUserStore()

const categories = ref<Category[]>([])
const unreadCount = ref(0)
const isScrolled = ref(false)
const searchQuery = ref('')

function onScroll() { isScrolled.value = window.scrollY > 20 }
onMounted(async () => {
  window.addEventListener('scroll', onScroll, { passive: true })
  try { categories.value = await getCategoriesApi() } catch { /* */ }
  if (userStore.isLoggedIn) fetchUnreadCount()
})
onUnmounted(() => window.removeEventListener('scroll', onScroll))

// 每次路由变化（比如从通知页返回），刷新未读数
watch(() => route.fullPath, () => {
  if (userStore.isLoggedIn) fetchUnreadCount()
})

async function fetchUnreadCount() {
  try { const r = await getUnreadCountApi(); unreadCount.value = r.count } catch { /* */ }
}
function onSearch() {
  if (!searchQuery.value.trim()) return
  router.push('/category?keyword=' + encodeURIComponent(searchQuery.value.trim()))
}
function goToNotifications() { router.push('/notifications') }
function goToUser() { router.push(userStore.isLoggedIn ? '/user' : '/login') }
function handleLogout() { userStore.logout(); router.push('/login'); ElMessage.success('已退出') }

defineEmits<{ 'open-cart': [] }>()
</script>

<style scoped>
.site-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: #1a1a1a;
  transition: background var(--transition);
}
.site-header.scrolled { background: rgba(26,26,26,0.97); backdrop-filter: blur(20px); }

.header-inner {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 0 32px;
  height: 64px;
  display: flex;
  align-items: center;
  gap: 40px;
}

.logo {
  font-family: var(--font-display);
  font-size: 22px;
  font-weight: 700;
  letter-spacing: 4px;
  color: #fff;
  flex-shrink: 0;
}

.main-nav { display: flex; align-items: center; gap: 4px; flex: 1; }
.nav-link {
  font-size: 13px;
  color: rgba(255,255,255,0.65);
  padding: 8px 16px;
  border-radius: 6px;
  transition: all var(--transition-fast);
  font-weight: 500;
  letter-spacing: 0.5px;
}
.nav-link:hover { color: #fff; background: rgba(255,255,255,0.08); }
.nav-link.active { color: #fff; font-weight: 600; }
.nav-link.accent { color: var(--accent); }

.header-right { display: flex; align-items: center; gap: 2px; flex-shrink: 0; }

/* Search */
.search-form {
  display: flex;
  align-items: center;
  gap: 8px;
  height: 36px;
  padding: 0 14px;
  border-radius: 8px;
  background: rgba(255,255,255,0.08);
  transition: background var(--transition-fast);
}
.search-form:focus-within { background: rgba(255,255,255,0.14); }
.search-icon { flex-shrink: 0; color: rgba(255,255,255,0.4); }
.search-input {
  border: none;
  background: transparent;
  outline: none;
  color: #fff;
  font-size: 13px;
  width: 140px;
}
.search-input::placeholder { color: rgba(255,255,255,0.35); }

.icon-btn {
  position: relative;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  color: rgba(255,255,255,0.65);
  transition: all var(--transition-fast);
}
.icon-btn:hover { background: rgba(255,255,255,0.08); color: #fff; }

.dot {
  position: absolute;
  top: 4px;
  right: 4px;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 9px;
  background: var(--accent);
  color: #1a1a1a;
  font-size: 10px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}

@media (max-width: 768px) {
  .main-nav { display: none; }
  .header-inner { padding: 0 16px; gap: 0; }
  .header-right .nav-link { display: none; }
}
</style>
