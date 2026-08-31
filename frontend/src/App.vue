<script setup lang="ts">
import { ref, computed } from 'vue'
import { RouterView, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import LayoutHeader from '@/components/layout/LayoutHeader.vue'
import LayoutFooter from '@/components/layout/LayoutFooter.vue'
import CartSidebar from '@/components/cart/CartSidebar.vue'
import ToastNotification from '@/components/common/ToastNotification.vue'
import AiChatPanel from '@/components/ai/AiChatPanel.vue'

const route = useRoute()
const userStore = useUserStore()
const isAdminRoute = computed(() => route.path.startsWith('/admin'))
const isAuthPage = computed(() => route.path === '/login' || route.path === '/register')
const isAdmin = computed(() => userStore.isAdmin)
const showAi = computed(() => !isAdminRoute.value && !isAuthPage.value && !isAdmin.value)

const cartVisible = ref(false)
const toastRef = ref<InstanceType<typeof ToastNotification> | null>(null)

function openCart() {
  cartVisible.value = true
}

function showToast(msg: string) {
  toastRef.value?.show(msg)
}

// Provide toast to all child components
import { provide } from 'vue'
provide('showToast', showToast)
</script>

<template>
  <LayoutHeader v-if="!isAdminRoute && !isAuthPage && !isAdmin" @open-cart="openCart" />
  <main>
    <router-view v-slot="{ Component, route: r }">
      <keep-alive v-if="r.meta.keepAlive">
        <component :is="Component" :key="r.fullPath" />
      </keep-alive>
      <component v-else :is="Component" :key="r.fullPath" />
    </router-view>
  </main>
  <LayoutFooter v-if="!isAdminRoute && !isAuthPage && !isAdmin" />
  <CartSidebar v-if="!isAdminRoute && !isAuthPage && !isAdmin" v-model="cartVisible" />

  <!-- AI 导购（浮动按钮 + 侧边抽屉） -->
  <AiChatPanel v-if="showAi" />

  <ToastNotification ref="toastRef" />
</template>

<style>
#app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}
main {
  flex: 1;
}
</style>
