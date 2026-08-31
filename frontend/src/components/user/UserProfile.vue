<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useUserStore } from '@/stores/user'
import request from '@/api/request'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const isEditing = ref(false)
const uploadingAvatar = ref(false)

const form = reactive({
  nickname: userStore.userInfo?.nickname || '',
  email: userStore.userInfo?.email || '',
  phone: userStore.userInfo?.phone || '',
})

function startEdit() {
  form.nickname = userStore.userInfo?.nickname || ''
  form.email = userStore.userInfo?.email || ''
  form.phone = userStore.userInfo?.phone || ''
  isEditing.value = true
}

function cancelEdit() { isEditing.value = false }

async function saveProfile() {
  try {
    await userStore.updateProfile({ nickname: form.nickname, email: form.email, phone: form.phone })
    isEditing.value = false
    ElMessage.success('资料更新成功')
  } catch { ElMessage.error('更新失败') }
}

async function onAvatarChange(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  uploadingAvatar.value = true
  try {
    const formData = new FormData(); formData.append('file', file)
    const res = await request.post('/upload', formData)
    let url = res
    if (typeof res === 'object' && res !== null) url = (res as any).data ?? res
    // 直接更新本地状态 + localStorage 立即生效
    userStore.setAvatar(String(url))
    ElMessage.success('头像已更新')
  } catch { ElMessage.error('上传失败') }
  uploadingAvatar.value = false
}
</script>

<template>
  <div class="profile-card">
    <div class="profile-header">
      <div class="avatar-wrap" :class="{ uploading: uploadingAvatar }">
        <img v-if="userStore.userInfo?.avatar" :src="userStore.userInfo.avatar" class="avatar-img" />
        <span v-else class="avatar-text">{{ (userStore.userInfo?.nickname || userStore.userInfo?.username || 'U').charAt(0).toUpperCase() }}</span>
        <label class="avatar-upload">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/><circle cx="12" cy="13" r="4"/></svg>
          <input type="file" accept="image/*" hidden @change="onAvatarChange" />
        </label>
      </div>
      <div class="profile-header-info" v-if="!isEditing">
        <h3>{{ userStore.userInfo?.nickname || userStore.userInfo?.username || '用户' }}</h3>
        <span class="profile-role">会员</span>
      </div>
    </div>

    <div class="profile-body" v-if="!isEditing">
      <div class="info-row"><span class="info-label">用户名</span><span class="info-value">{{ userStore.userInfo?.username }}</span></div>
      <div class="info-row"><span class="info-label">昵称</span><span class="info-value">{{ userStore.userInfo?.nickname || '-' }}</span></div>
      <div class="info-row"><span class="info-label">邮箱</span><span class="info-value">{{ userStore.userInfo?.email || '-' }}</span></div>
      <div class="info-row"><span class="info-label">手机号</span><span class="info-value">{{ userStore.userInfo?.phone || '-' }}</span></div>
      <button class="edit-btn" @click="startEdit">编辑资料</button>
    </div>

    <div class="profile-body" v-else>
      <el-form :model="form" label-width="80px" size="default">
        <el-form-item label="昵称"><el-input v-model="form.nickname" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
      </el-form>
      <div class="form-actions">
        <button class="save-btn" @click="saveProfile">保存</button>
        <button class="cancel-btn" @click="cancelEdit">取消</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.profile-card { background: #fff; border-radius: 16px; border: 1px solid var(--border); overflow: hidden; }
.profile-header {
  display: flex; align-items: center; gap: 18px; padding: 28px 28px 20px;
  background: linear-gradient(135deg, #f5f0e8, #ede4d3);
}
.avatar-wrap {
  width: 72px; height: 72px; border-radius: 50%; flex-shrink: 0; position: relative;
  display: flex; align-items: center; justify-content: center; overflow: hidden;
  background: linear-gradient(135deg, var(--accent), #c0392b);
}
.avatar-wrap.uploading { opacity: 0.6; }
.avatar-img { width: 100%; height: 100%; object-fit: cover; }
.avatar-text { font-size: 28px; font-weight: 700; color: #fff; }
.avatar-upload {
  position: absolute; bottom: 0; left: 0; right: 0; height: 24px;
  background: rgba(0,0,0,0.5); color: #fff; display: flex; align-items: center; justify-content: center;
  cursor: pointer; opacity: 0; transition: opacity .2s;
}
.avatar-wrap:hover .avatar-upload { opacity: 1; }
.profile-header-info h3 { font-size: 20px; font-weight: 700; margin: 0 0 4px; }
.profile-role { font-size: 12px; color: var(--accent); border: 1px solid var(--accent); padding: 2px 12px; border-radius: 12px; }
.profile-body { padding: 24px 28px 28px; }
.info-row { display: flex; justify-content: space-between; align-items: center; padding: 12px 0; border-bottom: 1px solid var(--border); }
.info-row:last-of-type { border-bottom: none; margin-bottom: 20px; }
.info-label { font-size: 13px; color: var(--text-muted); }
.info-value { font-size: 14px; color: var(--primary); font-weight: 500; }
.edit-btn { width: 100%; padding: 12px; border: 1px solid var(--primary); background: transparent; color: var(--primary); border-radius: 24px; font-size: 14px; font-weight: 600; cursor: pointer; font-family: inherit; letter-spacing: 1px; transition: all .3s; }
.edit-btn:hover { background: var(--primary); color: #fff; }
.form-actions { display: flex; gap: 12px; margin-top: 8px; }
.save-btn { flex: 1; padding: 12px; border: none; border-radius: 24px; background: var(--primary); color: #fff; font-size: 14px; font-weight: 600; cursor: pointer; font-family: inherit; }
.save-btn:hover { background: #1a1a1a; }
.cancel-btn { flex: 1; padding: 12px; border: 1px solid var(--border); border-radius: 24px; background: #fff; color: var(--text-muted); font-size: 14px; cursor: pointer; font-family: inherit; }
.cancel-btn:hover { border-color: var(--text-muted); color: var(--primary); }
</style>
