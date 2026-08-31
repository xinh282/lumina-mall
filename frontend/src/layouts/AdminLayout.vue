<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { computed } from 'vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

function handleSelect(path: string) {
  router.push(path)
}

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>

<template>
  <el-container class="admin-layout">
    <el-aside width="220px" class="admin-sidebar">
      <div class="sidebar-header">
        <router-link to="/" class="sidebar-logo">LUMINA</router-link>
        <span class="sidebar-badge">管理后台</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        background-color="#2d2d2d"
        text-color="rgba(255,255,255,0.7)"
        active-text-color="#c9a96e"
        @select="handleSelect"
      >
        <el-menu-item index="/admin/dashboard">
          <span>数据统计</span>
        </el-menu-item>
        <el-menu-item index="/admin/products">
          <span>商品管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/orders">
          <span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/refunds">
          <span>退款管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/coupons">
          <span>优惠券管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/reviews">
          <span>评价管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/users">
          <span>用户管理</span>
        </el-menu-item>
      </el-menu>
      <div class="sidebar-footer">
        <span class="admin-name">{{ userStore.userInfo?.nickname || '管理员' }}</span>
        <el-button text size="small" @click="handleLogout" style="color: rgba(255,255,255,0.6)">退出</el-button>
      </div>
    </el-aside>
    <el-container>
      <el-main class="admin-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.admin-layout {
  min-height: 100vh;
}
.admin-sidebar {
  background: var(--primary);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.sidebar-header {
  padding: 24px 20px 16px;
  text-align: center;
}
.sidebar-logo {
  font-size: 22px;
  font-weight: 700;
  letter-spacing: 3px;
  color: #fff;
  text-decoration: none;
  display: block;
}
.sidebar-badge {
  font-size: 11px;
  color: var(--accent);
  letter-spacing: 2px;
  margin-top: 4px;
  display: block;
}
.sidebar-footer {
  margin-top: auto;
  padding: 16px 20px;
  border-top: 1px solid rgba(255,255,255,0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.admin-name {
  color: rgba(255,255,255,0.8);
  font-size: 13px;
}
.admin-main {
  background: var(--bg);
  min-height: 100vh;
}
.el-menu {
  border-right: none;
}
</style>
