<template>
  <div class="brand-page">
    <!-- Hero -->
    <section class="brand-hero">
      <div class="hero-overlay" />
      <div class="hero-content">
        <span class="hero-eyebrow">SINCE 2015</span>
        <h1>以匠心，致生活</h1>
        <p>LUMINA 诞生于对美好生活的执着追求，我们相信每一件物品都承载着美学与温度。</p>
      </div>
    </section>

    <!-- Mission -->
    <section class="mission-section">
      <div class="content-inner">
        <div class="mission-text">
          <h2>我们的故事</h2>
          <p>
            LUMINA 创立于 2015 年，源自创始人 Lina Chen 在米兰设计周上的一次灵感碰撞。她发现，
            真正打动人心的设计，从不喧哗，却在细节处流露温度。于是她决定打造一个品牌，
            将全球独立设计师的匠心之作带给追求品质生活的人们。
          </p>
          <p>
            从最初的三位设计师合作，到今天与超过 50 位全球独立设计师携手，
            LUMINA 始终坚持「少即是多」的选品哲学——每一件上线商品都经过
            材质、工艺、美学三重严选。
          </p>
        </div>
        <div class="mission-values">
          <div class="value-item">
            <div class="value-num">10+</div>
            <div class="value-label">国家/地区</div>
          </div>
          <div class="value-item">
            <div class="value-num">50+</div>
            <div class="value-label">合作设计师</div>
          </div>
          <div class="value-item">
            <div class="value-num">200K+</div>
            <div class="value-label">品质用户</div>
          </div>
          <div class="value-item">
            <div class="value-num">99.6%</div>
            <div class="value-label">好评率</div>
          </div>
        </div>
      </div>
    </section>

    <!-- Timeline -->
    <section class="timeline-section">
      <div class="content-inner">
        <h2>品牌历程</h2>
        <div class="timeline">
          <div class="tl-item">
            <div class="tl-year">2015</div>
            <div class="tl-dot" />
            <div class="tl-card">
              <h4>品牌创立</h4>
              <p>Lina 在米兰发布首个设计师合作系列，LUMINA 正式成立</p>
            </div>
          </div>
          <div class="tl-item">
            <div class="tl-year">2017</div>
            <div class="tl-dot" />
            <div class="tl-card">
              <h4>首个线下概念店</h4>
              <p>上海新天地开设首家旗舰体验空间，融合零售与生活方式</p>
            </div>
          </div>
          <div class="tl-item">
            <div class="tl-year">2019</div>
            <div class="tl-dot" />
            <div class="tl-card">
              <h4>全球设计师联名计划</h4>
              <p>启动「LUMINA x Designer」联名系列，与 20 位独立设计师深度合作</p>
            </div>
          </div>
          <div class="tl-item">
            <div class="tl-year">2022</div>
            <div class="tl-dot" />
            <div class="tl-card">
              <h4>可持续时尚倡议</h4>
              <p>发布「Green Thread」可持续计划，承诺 2030 年前实现全线环保包装</p>
            </div>
          </div>
          <div class="tl-item">
            <div class="tl-year">2025</div>
            <div class="tl-dot" />
            <div class="tl-card">
              <h4>全渠道数字化</h4>
              <p>线上商城全面升级，实现 AI 个性化推荐与虚拟试穿体验</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Philosophy -->
    <section class="philosophy-section">
      <div class="content-inner">
        <h2>品牌哲学</h2>
        <div class="philosophy-grid">
          <div class="phil-card">
            <div class="phil-icon">◆</div>
            <h4>严选材质</h4>
            <p>每一块面料、每一颗纽扣，都经过实验室级检测。我们只选用通过 OEKO-TEX 认证的环保材料。</p>
          </div>
          <div class="phil-card">
            <div class="phil-icon">◇</div>
            <h4>尊重工艺</h4>
            <p>与全球手工艺人合作，保留传统技法。从日本蓝染到意大利植鞣，每一件都是时间的作品。</p>
          </div>
          <div class="phil-card">
            <div class="phil-icon">◈</div>
            <h4>设计即态度</h4>
            <p>不追逐潮流，不制造浪费。我们的设计经得起时间考验，跨越季节与趋势。</p>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
</script>

<style scoped>
.brand-page {
  min-height: 100vh;
  background: var(--bg);
}

/* Hero */
.brand-hero {
  position: relative;
  height: 520px;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 40%, #0f3460 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}
.hero-overlay {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(ellipse at 20% 50%, rgba(180, 140, 60, 0.15) 0%, transparent 50%),
    radial-gradient(ellipse at 80% 50%, rgba(180, 140, 60, 0.1) 0%, transparent 50%);
}
.hero-content {
  position: relative;
  text-align: center;
  max-width: 640px;
  padding: 0 24px;
}
.hero-eyebrow {
  font-size: 12px;
  letter-spacing: 6px;
  color: var(--accent);
  text-transform: uppercase;
}
.hero-content h1 {
  font-size: 48px;
  font-weight: 300;
  color: #fff;
  margin: 16px 0 20px;
  letter-spacing: 4px;
  font-family: var(--font-serif, serif);
}
.hero-content p {
  font-size: 15px;
  color: rgba(255, 255, 255, 0.55);
  line-height: 2;
  margin: 0;
}

/* Mission */
.mission-section {
  padding: 80px 0 64px;
}
.content-inner {
  max-width: 880px;
  margin: 0 auto;
  padding: 0 24px;
}
.mission-text h2 {
  font-size: 28px;
  font-weight: 300;
  letter-spacing: 2px;
  margin: 0 0 24px;
  font-family: var(--font-serif, serif);
}
.mission-text p {
  font-size: 15px;
  line-height: 2;
  color: var(--text-muted);
  margin: 0 0 16px;
}
.mission-values {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
  margin-top: 48px;
  padding-top: 48px;
  border-top: 1px solid var(--border);
}
.value-item {
  text-align: center;
}
.value-num {
  font-size: 32px;
  font-weight: 300;
  color: var(--accent);
  font-family: var(--font-serif, serif);
}
.value-label {
  font-size: 13px;
  color: var(--text-muted);
  margin-top: 4px;
}

/* Timeline */
.timeline-section {
  padding: 64px 0;
  background: #fafaf8;
}
.timeline-section h2 {
  font-size: 28px;
  font-weight: 300;
  letter-spacing: 2px;
  margin: 0 0 48px;
  text-align: center;
  font-family: var(--font-serif, serif);
}
.timeline {
  display: flex;
  flex-direction: column;
  gap: 0;
}
.tl-item {
  display: grid;
  grid-template-columns: 80px 32px 1fr;
  align-items: start;
  gap: 16px;
  padding-bottom: 32px;
  position: relative;
}
.tl-item:not(:last-child)::after {
  content: '';
  position: absolute;
  left: 95px;
  top: 32px;
  bottom: 0;
  width: 1px;
  background: var(--border);
}
.tl-year {
  font-size: 14px;
  font-weight: 600;
  color: var(--accent);
  text-align: right;
  padding-top: 4px;
}
.tl-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: var(--accent);
  margin-top: 8px;
  flex-shrink: 0;
}
.tl-card {
  background: #fff;
  border: 1px solid var(--border);
  border-radius: var(--radius);
  padding: 20px 24px;
}
.tl-card h4 {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 6px;
}
.tl-card p {
  font-size: 14px;
  color: var(--text-muted);
  margin: 0;
  line-height: 1.6;
}

/* Philosophy */
.philosophy-section {
  padding: 80px 0;
}
.philosophy-section h2 {
  font-size: 28px;
  font-weight: 300;
  letter-spacing: 2px;
  margin: 0 0 48px;
  text-align: center;
  font-family: var(--font-serif, serif);
}
.philosophy-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 32px;
}
.phil-card {
  text-align: center;
  padding: 40px 24px;
}
.phil-icon {
  font-size: 28px;
  color: var(--accent);
  margin-bottom: 16px;
}
.phil-card h4 {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 12px;
  letter-spacing: 1px;
}
.phil-card p {
  font-size: 14px;
  color: var(--text-muted);
  line-height: 1.8;
  margin: 0;
}

@media (max-width: 768px) {
  .brand-hero {
    height: 400px;
  }
  .hero-content h1 {
    font-size: 32px;
  }
  .mission-values {
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
  }
  .philosophy-grid {
    grid-template-columns: 1fr;
  }
  .tl-item {
    grid-template-columns: 60px 24px 1fr;
    gap: 12px;
  }
  .tl-item:not(:last-child)::after {
    left: 71px;
  }
}
</style>
