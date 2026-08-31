<template>
  <div class="not-found">
    <h1>404</h1>
    <p>页面不存在</p>
    <el-button type="primary" size="large" @click="goHome">
      返回首页
    </el-button>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

function goHome() {
  router.push('/')
}
</script>

<style scoped>
.not-found {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  padding: 48px 24px;
  text-align: center;
}

.not-found h1 {
  font-size: 72px;
  font-weight: 700;
  color: var(--accent);
  margin: 0 0 8px;
  line-height: 1;
  font-family: var(--font-serif, serif);
  letter-spacing: 4px;
}

.not-found p {
  font-size: 18px;
  color: var(--text-muted);
  margin: 0 0 24px;
}

.not-found .el-button {
  height: 44px;
  font-size: 15px;
  background-color: var(--primary);
  border-color: var(--primary);
  padding: 0 36px;
  border-radius: var(--radius, 10px);
  letter-spacing: 1px;
}

.not-found .el-button:hover {
  opacity: 0.9;
}
</style>
