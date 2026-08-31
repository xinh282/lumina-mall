<template>
  <div class="notification-page">
    <div class="notification-container">
      <div class="page-header">
        <h1 class="page-title">消息通知</h1>
        <el-button v-if="notifications.length" text type="primary" @click="handleMarkAllRead">全部已读</el-button>
      </div>

      <el-empty v-if="!loading && !notifications.length" description="暂无通知" />

      <div v-else class="notification-list">
        <div
          v-for="n in notifications"
          :key="n.id"
          class="notification-item"
          :class="{ unread: n.isRead === 0 }"
          @click="handleClick(n)"
        >
          <div class="notif-left">
            <span class="notif-dot" v-if="n.isRead === 0"></span>
            <div class="notif-content">
              <p class="notif-title">{{ n.title }}</p>
              <p class="notif-body">{{ n.content }}</p>
              <p class="notif-time">{{ n.createTime }}</p>
            </div>
          </div>
          <el-tag v-if="n.type" size="small" type="info">{{ typeLabel(n.type) }}</el-tag>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getNotificationsApi, markReadApi, markAllReadApi } from '@/api/notification'
import type { Notification } from '@/api/notification'

const router = useRouter()
const notifications = ref<Notification[]>([])
const loading = ref(true)

onMounted(async () => {
  try {
    notifications.value = await getNotificationsApi()
  } catch { /* ignore */ }
  loading.value = false
})

async function handleClick(n: Notification) {
  if (n.isRead === 0) {
    try {
      await markReadApi(n.id)
      n.isRead = 1
    } catch { /* ignore */ }
  }
  if (n.type === 'ORDER' && n.refId) {
    router.push('/user')
  }
}

async function handleMarkAllRead() {
  try {
    await markAllReadApi()
    notifications.value.forEach(n => n.isRead = 1)
    ElMessage.success('已全部标记为已读')
  } catch { /* ignore */ }
}

function typeLabel(type: string) {
  const map: Record<string, string> = { ORDER: '订单', REFUND: '退款', SYSTEM: '系统' }
  return map[type] || type
}
</script>

<style scoped>
.notification-page {
  background: var(--bg);
  min-height: calc(100vh - 120px);
}

.notification-container {
  max-width: 720px;
  margin: 0 auto;
  padding: 48px 24px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 32px;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--text);
  margin: 0;
  letter-spacing: 1px;
}

.notification-list {
  display: flex;
  flex-direction: column;
  gap: 1px;
  background: var(--border);
  border-radius: var(--radius, 10px);
  overflow: hidden;
}

.notification-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background: #fff;
  cursor: pointer;
  transition: background var(--transition);
}

.notification-item:hover {
  background: var(--bg);
}

.notification-item.unread {
  background: #faf8f5;
}

.notif-left {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  flex: 1;
  min-width: 0;
}

.notif-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--accent);
  flex-shrink: 0;
  margin-top: 6px;
}

.notif-content {
  flex: 1;
  min-width: 0;
}

.notif-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text);
  margin: 0 0 4px;
}

.notif-body {
  font-size: 13px;
  color: var(--text-muted);
  margin: 0 0 4px;
}

.notif-time {
  font-size: 12px;
  color: var(--text-muted);
  margin: 0;
}
</style>
