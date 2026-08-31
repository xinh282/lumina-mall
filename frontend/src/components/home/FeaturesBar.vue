<script setup lang="ts">
interface Feature {
  title: string
  subtitle: string
  icon: string
}

const features: Feature[] = [
  { title: '免费配送', subtitle: '满299元享全国包邮', icon: 'truck' },
  { title: '品质保证', subtitle: '7天无理由退换', icon: 'shield' },
  { title: '会员专属', subtitle: '积分加倍，福利不断', icon: 'crown' },
  { title: '客服支持', subtitle: '全天候在线响应', icon: 'headset' },
]
</script>

<template>
  <section class="features">
    <div class="features-grid">
      <div v-for="f in features" :key="f.title" class="feature-card">
        <div class="feature-icon">
          <svg v-if="f.icon === 'truck'" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M1 3h15v13H1zM16 8h4l3 4v4h-2.5M1 16h.01"/>
            <circle cx="5.5" cy="18.5" r="2.5"/>
            <circle cx="18.5" cy="18.5" r="2.5"/>
          </svg>
          <svg v-if="f.icon === 'shield'" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
          </svg>
          <svg v-if="f.icon === 'crown'" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M2 4l3 12h14l3-12-6 4-4-8-4 8-6-4z"/>
            <path d="M4 20h16v2H4z"/>
          </svg>
          <svg v-if="f.icon === 'headset'" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M3 18v-6a9 9 0 0118 0v6"/>
            <path d="M21 18a2 2 0 01-2 2h-2v-4h2a2 2 0 012 2z"/>
            <path d="M3 18a2 2 0 002 2h2v-4H5a2 2 0 00-2 2z"/>
          </svg>
        </div>
        <div class="feature-text">
          <div class="feature-title">{{ f.title }}</div>
          <div class="feature-subtitle">{{ f.subtitle }}</div>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.features {
  padding: 48px 24px;
  background: var(--bg);
  border-bottom: 1px solid var(--border);
}
.features-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
  max-width: 1100px;
  margin: 0 auto;
}
.feature-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: white;
  border-radius: 12px;
  border: 1px solid var(--border);
  transition: all 0.3s ease;
}
.feature-card:hover {
  border-color: var(--accent);
  box-shadow: 0 4px 16px rgba(0,0,0,0.06);
}
.feature-icon {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  background: linear-gradient(135deg, #f5f0e8, #ede4d3);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--accent);
  flex-shrink: 0;
}
.feature-title {
  font-size: 15px;
  font-weight: 700;
  color: var(--primary);
  margin-bottom: 4px;
}
.feature-subtitle {
  font-size: 12px;
  color: var(--text-muted);
}
@media (max-width: 768px) {
  .features-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 480px) {
  .features-grid { grid-template-columns: 1fr; }
}
</style>
