<template>
  <div class="auth-page">
    <div class="auth-card">
      <h2>登录</h2>
      <p class="auth-subtitle">欢迎回到 LUMINA</p>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="用户名"
            size="large"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            size="large"
            show-password
          />
        </el-form-item>
        <el-button
          type="primary"
          native-type="submit"
          :loading="loading"
          size="large"
          class="submit-btn"
        >
          登录
        </el-button>
      </el-form>

      <p class="auth-link">
        还没有账号？<router-link to="/register">立即注册</router-link>
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
})

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6个字符', trigger: 'blur' },
  ],
}

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await userStore.login(form.username, form.password)
    ElMessage.success('登录成功')
    const redirect = userStore.isAdmin
      ? '/admin'
      : ((route.query.redirect as string) || '/')
    router.push(redirect)
  } catch (err: any) {
    const msg = err?.response?.data?.message || err?.message || '用户名或密码错误'
    ElMessage.error(msg)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 120px);
  padding: 48px 24px;
  background: var(--bg);
}

.auth-card {
  width: 100%;
  max-width: 420px;
  background: #fff;
  border-radius: var(--radius, 10px);
  box-shadow: var(--shadow-md);
  padding: 48px 40px;
}

.auth-card h2 {
  font-size: 28px;
  font-weight: 700;
  color: var(--text);
  text-align: center;
  margin: 0 0 8px;
  letter-spacing: 1px;
}

.auth-subtitle {
  font-size: 14px;
  color: var(--text-muted);
  text-align: center;
  margin: 0 0 40px;
}

.submit-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  background-color: var(--primary);
  border-color: var(--primary);
  border-radius: var(--radius, 10px);
  margin-top: 8px;
  letter-spacing: 2px;
}

.submit-btn:hover {
  opacity: 0.9;
}

.auth-link {
  font-size: 13px;
  color: var(--text-muted);
  text-align: center;
  margin: 28px 0 0;
}

.auth-link a {
  color: var(--accent);
  text-decoration: none;
  font-weight: 500;
}

.auth-link a:hover {
  text-decoration: underline;
}

@media (max-width: 480px) {
  .auth-card {
    padding: 36px 24px;
  }
}
</style>
