<template>
  <div class="review-section">
    <div class="review-header">
      <h3>商品评价 <span class="review-count">({{ total }})</span></h3>
      <div class="rating-summary">
        <div class="stars-big">
          <span v-for="s in 5" :key="s" class="star" :class="{ filled: s <= Math.round(avgRating) }">★</span>
        </div>
        <span class="avg-text">{{ avgRating.toFixed(1) }}</span>
      </div>
      <el-button size="small" type="primary" @click="showForm = true">写评价</el-button>
    </div>

    <!-- Review Form -->
    <div v-if="showForm" class="review-form">
      <div class="rating-input">
        <span class="rate-label">评分：</span>
        <span v-for="s in 5" :key="s" class="star-btn" :class="{ active: formRating >= s }" @click="formRating = s">★</span>
      </div>
      <el-input v-model="formContent" type="textarea" :rows="3" maxlength="200" show-word-limit placeholder="分享你的使用感受..." />
      <div class="form-actions">
        <el-button size="small" @click="showForm = false">取消</el-button>
        <el-button size="small" type="primary" :loading="submitting" @click="submitReview">提交评价</el-button>
      </div>
    </div>

    <!-- Review List -->
    <div v-if="reviews.length" class="review-list">
      <div v-for="r in reviews" :key="r.id" class="review-item">
        <div class="review-top">
          <span class="review-stars">
            <span v-for="s in 5" :key="s" class="s-star" :class="{ on: s <= r.rating }">★</span>
          </span>
          <span class="review-date">{{ formatDate(r.createTime) }}</span>
        </div>
        <p class="review-content">{{ r.content }}</p>
      </div>
    </div>
    <div v-else class="no-review">暂无评价，成为第一个评价的人吧</div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getReviewsApi, getRatingStatsApi, createReviewApi } from '@/api/review'
import type { Review } from '@/api/review'
import { formatDate } from '@/utils/format'

const props = defineProps<{ productId: number }>()

const reviews = ref<Review[]>([])
const total = ref(0)
const avgRating = ref(0)
const showForm = ref(false)
const formRating = ref(5)
const formContent = ref('')
const submitting = ref(false)

onMounted(async () => {
  try {
    const [r, s] = await Promise.all([
      getReviewsApi(props.productId),
      getRatingStatsApi(props.productId),
    ])
    reviews.value = r.records
    total.value = r.total
    avgRating.value = Number(s.avgRating) || 0
  } catch { /* */ }
})

async function submitReview() {
  if (!formContent.value.trim()) { ElMessage.warning('请输入评价内容'); return }
  submitting.value = true
  try {
    await createReviewApi({ productId: props.productId, rating: formRating.value, content: formContent.value })
    ElMessage.success('评价成功')
    showForm.value = false
    formContent.value = ''
    formRating.value = 5
    // 刷新
    const [r, s] = await Promise.all([
      getReviewsApi(props.productId),
      getRatingStatsApi(props.productId),
    ])
    reviews.value = r.records
    total.value = r.total
    avgRating.value = Number(s.avgRating) || 0
  } catch { ElMessage.error('评价失败') }
  submitting.value = false
}
</script>

<style scoped>
.review-section { margin-top: 40px; padding-top: 32px; border-top: 1px solid var(--border); }

.review-header { display: flex; align-items: center; gap: 16px; margin-bottom: 20px; flex-wrap: wrap; }
.review-header h3 { font-size: 18px; font-weight: 700; margin: 0; }
.review-count { font-weight: 400; color: var(--text-muted); }
.rating-summary { display: flex; align-items: center; gap: 6px; }
.stars-big { display: flex; }
.star { font-size: 18px; color: #e0e0e0; }
.star.filled { color: #f5a623; }
.avg-text { font-size: 15px; font-weight: 700; color: #f5a623; }

/* Form */
.review-form { background: var(--bg-warm); padding: 16px; border-radius: var(--radius-sm); margin-bottom: 20px; display: flex; flex-direction: column; gap: 12px; }
.rating-input { display: flex; align-items: center; gap: 4px; }
.rate-label { font-size: 14px; }
.star-btn { font-size: 24px; color: #e0e0e0; cursor: pointer; transition: color .15s; }
.star-btn.active { color: #f5a623; }
.form-actions { display: flex; gap: 8px; justify-content: flex-end; }

/* List */
.review-list { display: flex; flex-direction: column; gap: 16px; }
.review-item { padding-bottom: 16px; border-bottom: 1px solid var(--border-light); }
.review-top { display: flex; align-items: center; gap: 12px; margin-bottom: 6px; }
.review-stars { display: flex; }
.s-star { font-size: 13px; color: #e0e0e0; }
.s-star.on { color: #f5a623; }
.review-date { font-size: 12px; color: var(--text-muted); }
.review-content { font-size: 14px; line-height: 1.6; color: var(--text-secondary); margin: 0; }
.no-review { text-align: center; padding: 32px; color: var(--text-muted); font-size: 14px; }
</style>
