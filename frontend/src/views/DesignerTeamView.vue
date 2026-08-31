<template>
  <div class="designer-page">
    <div class="page-hero">
      <span class="hero-eyebrow">CREATIVE MINDS</span>
      <h1>设计师团队</h1>
      <p>来自全球的独立设计师，以独特视角重新定义日常之美</p>
    </div>

    <div class="content">
      <!-- Lead Designer -->
      <section class="lead-section">
        <div class="lead-card">
          <div class="lead-avatar">
            <div class="avatar-circle large">LC</div>
          </div>
          <div class="lead-info">
            <span class="role-badge">创始人 & 创意总监</span>
            <h2>Lina Chen</h2>
            <p class="lead-bio">
              毕业于中央圣马丁艺术与设计学院，曾任职于 Maison Margiela 与 Jil Sander。
              2015 年创立 LUMINA，致力于搭建全球独立设计师与中国消费者的桥梁。
              她的设计理念深受日本侘寂美学与北欧极简主义的影响——
              「真正的奢华，是克制的表达」。
            </p>
            <div class="lead-quote">
              "We don't chase trends. We curate pieces that outlast seasons."
            </div>
          </div>
        </div>
      </section>

      <!-- Designers Grid -->
      <section class="team-section">
        <h2>核心设计师</h2>
        <div class="designer-grid">
          <div v-for="d in designers" :key="d.name" class="designer-card">
            <div class="designer-avatar">
              <div class="avatar-circle" :style="{ background: d.gradient }">
                {{ d.initials }}
              </div>
            </div>
            <h4>{{ d.name }}</h4>
            <span class="designer-role">{{ d.role }}</span>
            <span class="designer-loc">{{ d.location }}</span>
            <p class="designer-bio">{{ d.bio }}</p>
          </div>
        </div>
      </section>

      <!-- Collaboration CTA -->
      <section class="cta-section">
        <h3>与 LUMINA 合作</h3>
        <p>我们始终在寻找富有才华的独立设计师。如果你有独特的创作理念，欢迎联系我们。</p>
        <el-button class="cta-btn" @click="$router.push('/contact')">
          联系团队
        </el-button>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
const designers = [
  {
    name: 'Marco Bellini',
    initials: 'MB',
    role: '高级女装设计师',
    location: '意大利 · 米兰',
    gradient: 'linear-gradient(135deg, #b8943d, #d4a853)',
    bio: '曾在 Bottega Veneta 担任高级设计师 8 年，擅长以建筑线条塑造服装结构，作品多次登上米兰时装周。',
  },
  {
    name: 'Yuki Tanaka',
    initials: 'YT',
    role: '面料与工艺总监',
    location: '日本 · 京都',
    gradient: 'linear-gradient(135deg, #2d5a4b, #4a8c6f)',
    bio: '出身京都染织世家，第四代传人。专注于天然染料与传统手工织造技艺的现代化应用，主导品牌环保面料研发。',
  },
  {
    name: 'Emma Westerberg',
    initials: 'EW',
    role: '配饰与生活方式设计师',
    location: '瑞典 · 斯德哥尔摩',
    gradient: 'linear-gradient(135deg, #5b6e8a, #7a8fa6)',
    bio: '北欧极简主义倡导者，作品涵盖包袋、家居器皿与香氛。曾获 Wallpaper* Design Awards 年度新锐设计师。',
  },
  {
    name: 'Pierre Dubois',
    initials: 'PD',
    role: '男装系列主设计师',
    location: '法国 · 巴黎',
    gradient: 'linear-gradient(135deg, #3d4f5f, #5a6e7e)',
    bio: '前 Hermès 男装部核心成员，专注经典男装的当代演绎。擅长将法式优雅与实用主义完美融合。',
  },
  {
    name: 'Anika Verma',
    initials: 'AV',
    role: '珠宝与首饰设计师',
    location: '印度 · 斋浦尔',
    gradient: 'linear-gradient(135deg, #8b5e3c, #a67c52)',
    bio: '印度传统 Kundan 工艺传承者，作品融合莫卧儿王朝珠宝美学与现代极简线条，被 Vogue India 评为年度设计师。',
  },
  {
    name: 'Sofia Andersen',
    initials: 'SA',
    role: '可持续时尚顾问',
    location: '丹麦 · 哥本哈根',
    gradient: 'linear-gradient(135deg, #6b4e5a, #8a6370)',
    bio: '哥本哈根时尚峰会常驻嘉宾，协助品牌制定可持续发展路线图，推动循环时尚在供应链中的实践。',
  },
]
</script>

<style scoped>
.designer-page {
  min-height: 100vh;
  background: var(--bg);
}

.page-hero {
  background: linear-gradient(135deg, #f5f0e8, #ede4d3);
  text-align: center;
  padding: 64px 24px 48px;
}
.hero-eyebrow {
  font-size: 12px;
  letter-spacing: 6px;
  color: var(--accent);
}
.page-hero h1 {
  font-size: 32px;
  font-weight: 300;
  letter-spacing: 2px;
  margin: 12px 0 8px;
  font-family: var(--font-serif, serif);
}
.page-hero p {
  color: var(--text-muted);
  font-size: 14px;
  margin: 0;
}

.content {
  max-width: 960px;
  margin: 0 auto;
  padding: 56px 24px;
}

/* Lead */
.lead-card {
  display: flex;
  gap: 40px;
  align-items: flex-start;
  padding: 48px;
  background: #fff;
  border: 1px solid var(--border);
  border-radius: var(--radius);
}
.lead-avatar {
  flex-shrink: 0;
}
.avatar-circle {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 24px;
  font-weight: 600;
  letter-spacing: 1px;
  background: linear-gradient(135deg, #1a1a2e, #2d3a4a);
}
.avatar-circle.large {
  width: 120px;
  height: 120px;
  font-size: 36px;
}
.role-badge {
  display: inline-block;
  font-size: 11px;
  letter-spacing: 3px;
  color: var(--accent);
  text-transform: uppercase;
  margin-bottom: 8px;
}
.lead-info h2 {
  font-size: 26px;
  font-weight: 300;
  margin: 0 0 16px;
  font-family: var(--font-serif, serif);
  letter-spacing: 1px;
}
.lead-bio {
  font-size: 15px;
  line-height: 2;
  color: var(--text-muted);
  margin: 0 0 20px;
}
.lead-quote {
  font-size: 15px;
  font-style: italic;
  color: var(--accent);
  padding-left: 16px;
  border-left: 2px solid var(--accent);
  font-family: var(--font-serif, serif);
}

/* Team */
.team-section {
  margin-top: 64px;
}
.team-section h2 {
  font-size: 24px;
  font-weight: 300;
  letter-spacing: 2px;
  margin: 0 0 32px;
  text-align: center;
  font-family: var(--font-serif, serif);
}
.designer-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}
.designer-card {
  text-align: center;
  padding: 32px 20px;
  background: #fff;
  border: 1px solid var(--border);
  border-radius: var(--radius);
  transition: box-shadow var(--transition);
}
.designer-card:hover {
  box-shadow: var(--shadow-md);
}
.designer-avatar {
  margin-bottom: 16px;
}
.designer-avatar .avatar-circle {
  width: 72px;
  height: 72px;
  font-size: 20px;
  margin: 0 auto;
}
.designer-card h4 {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 4px;
}
.designer-role {
  display: block;
  font-size: 13px;
  color: var(--accent);
  margin-bottom: 2px;
}
.designer-loc {
  display: block;
  font-size: 12px;
  color: var(--text-muted);
  margin-bottom: 12px;
}
.designer-bio {
  font-size: 13px;
  color: var(--text-muted);
  line-height: 1.7;
  margin: 0;
}

/* CTA */
.cta-section {
  margin-top: 80px;
  text-align: center;
  padding: 56px 24px;
  background: #fafaf8;
  border-radius: var(--radius);
}
.cta-section h3 {
  font-size: 22px;
  font-weight: 300;
  letter-spacing: 2px;
  margin: 0 0 12px;
  font-family: var(--font-serif, serif);
}
.cta-section p {
  font-size: 14px;
  color: var(--text-muted);
  margin: 0 0 24px;
}
.cta-btn {
  height: 44px;
  padding: 0 36px;
  font-size: 14px;
  letter-spacing: 2px;
  border: 1px solid var(--primary);
  background: transparent;
  color: var(--primary);
  border-radius: 0;
}
.cta-btn:hover {
  background: var(--primary);
  color: #fff;
}

@media (max-width: 768px) {
  .lead-card {
    flex-direction: column;
    padding: 32px 24px;
    text-align: center;
  }
  .lead-avatar {
    margin: 0 auto;
  }
  .lead-quote {
    text-align: left;
  }
  .designer-grid {
    grid-template-columns: 1fr 1fr;
  }
}
@media (max-width: 480px) {
  .designer-grid {
    grid-template-columns: 1fr;
  }
}
</style>
