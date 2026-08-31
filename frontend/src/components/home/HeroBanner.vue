<script setup lang="ts">
import { useRouter } from 'vue-router'
const router = useRouter()
</script>

<template>
  <section class="hero">
    <div class="hero-media">
      <div class="hero-image"></div>
    </div>
    <div class="hero-overlay">
      <div class="hero-text">
        <h1 class="hero-title">生活之美<br />在于细节</h1>
        <p class="hero-sub">甄选全球独立设计品牌，为日常注入品质与温度</p>
        <button class="hero-cta" @click="router.push('/category')">
          探索系列
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M5 12h14"/><path d="m12 5 7 7-7 7"/></svg>
        </button>
      </div>
    </div>
  </section>
</template>

<style scoped>
.hero {
  position: relative;
  height: 400px;
  display: flex;
  align-items: center;
  overflow: hidden;
}

.hero-media {
  position: absolute;
  inset: 0;
}

.hero-image {
  width: 100%;
  height: 100%;
  background: linear-gradient(160deg, #2a2520 0%, #352e28 40%, #2a2520 100%);
  position: relative;
}
.hero-image::after {
  content: '';
  position: absolute;
  top: -30%;
  right: -5%;
  width: 500px;
  height: 500px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(201,169,110,0.06) 0%, transparent 70%);
  pointer-events: none;
}

.hero-overlay {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 0 32px;
}

.hero-text {
  max-width: 540px;
}

.hero-title {
  font-family: var(--font-display);
  font-size: 44px;
  font-weight: 700;
  line-height: 1.15;
  color: #fff;
  margin-bottom: 16px;
  letter-spacing: 1px;
}

.hero-sub {
  font-size: 17px;
  line-height: 1.7;
  color: rgba(255,255,255,0.6);
  margin-bottom: 36px;
  max-width: 420px;
}

.hero-cta {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 15px 36px;
  border: none;
  background: #fff;
  color: #1a1a1a;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 0.5px;
  cursor: pointer;
  transition: all var(--transition);
  font-family: inherit;
}
.hero-cta:hover {
  background: var(--accent);
  color: #fff;
  transform: translateY(-2px);
}

@media (max-width: 768px) {
  .hero { height: 420px; }
  .hero-title { font-size: 36px; }
  .hero-overlay { padding: 0 20px; }
}
</style>
