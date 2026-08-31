<template>
  <footer class="layout-footer">
    <div class="footer-inner">
      <!-- Column 1: Brand -->
      <div class="footer-col brand-col">
        <h3 class="footer-logo">
          <span class="logo-main">LUMI</span><span class="logo-accent">NA</span>
        </h3>
        <p class="brand-desc">
          以匠心致生活。LUMINA致力于为您甄选全球高品质设计单品，让每一件物品都承载美学与温度。
        </p>
        <form class="newsletter-form" @submit.prevent="onSubscribe">
          <input
            v-model="email"
            type="email"
            class="newsletter-input"
            placeholder="输入邮箱订阅资讯"
            required
          />
          <button type="submit" class="newsletter-btn">订阅</button>
        </form>
      </div>

      <!-- Column 2 -->
      <div class="footer-col">
        <h4 class="footer-title">购物指南</h4>
        <ul class="footer-links">
          <li><router-link to="/size-guide">尺码指南</router-link></li>
          <li><router-link to="/shipping">配送说明</router-link></li>
          <li><router-link to="/returns">退换政策</router-link></li>
          <li><router-link to="/payment">支付方式</router-link></li>
        </ul>
      </div>

      <!-- Column 3 -->
      <div class="footer-col">
        <h4 class="footer-title">关于我们</h4>
        <ul class="footer-links">
          <li><router-link to="/brand">品牌故事</router-link></li>
          <li><router-link to="/designers">设计师团队</router-link></li>
          <li><router-link to="/stores">线下门店</router-link></li>
          <li><router-link to="/careers">加入我们</router-link></li>
        </ul>
      </div>

      <!-- Column 4 -->
      <div class="footer-col">
        <h4 class="footer-title">客户服务</h4>
        <ul class="footer-links">
          <li><router-link to="/faq">常见问题</router-link></li>
          <li><router-link to="/contact">联系我们</router-link></li>
          <li><router-link to="/privacy">隐私政策</router-link></li>
          <li><router-link to="/terms">使用条款</router-link></li>
        </ul>
      </div>
    </div>

    <!-- Bottom bar -->
    <div class="footer-bottom">
      <p>&copy; 2026 LUMINA. All rights reserved.</p>
    </div>
  </footer>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const email = ref('')

function onSubscribe() {
  if (email.value) {
    // In production, call API to subscribe
    console.log('Subscribing email:', email.value)
    email.value = ''
    alert('感谢订阅！')
  }
}
</script>

<style scoped>
.layout-footer {
  background: var(--primary);
  color: rgba(255, 255, 255, 0.7);
  padding: 64px 24px 0;
}

.footer-inner {
  max-width: var(--max-width);
  margin: 0 auto;
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr;
  gap: 48px;
  padding-bottom: 48px;
}

/* Brand column */
.footer-logo {
  font-family: var(--font-serif);
  font-size: 22px;
  font-weight: 700;
  letter-spacing: 3px;
  margin: 0 0 16px;
}
.logo-main {
  color: #fff;
}
.logo-accent {
  color: var(--accent);
}

.brand-desc {
  font-size: 14px;
  line-height: 1.8;
  margin: 0 0 20px;
  color: rgba(255, 255, 255, 0.55);
  max-width: 340px;
}

.newsletter-form {
  display: flex;
  gap: 8px;
}

.newsletter-input {
  flex: 1;
  padding: 10px 14px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: var(--radius);
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
  font-size: 13px;
  outline: none;
  transition: border-color var(--transition);
}
.newsletter-input::placeholder {
  color: rgba(255, 255, 255, 0.4);
}
.newsletter-input:focus {
  border-color: var(--accent);
}

.newsletter-btn {
  padding: 10px 20px;
  background: var(--accent);
  color: #fff;
  border: none;
  border-radius: var(--radius);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition);
  white-space: nowrap;
}
.newsletter-btn:hover {
  background: #b8943d;
}

/* Link columns */
.footer-title {
  font-size: 15px;
  font-weight: 600;
  color: #fff;
  margin: 0 0 16px;
}

.footer-links {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.footer-links a {
  color: rgba(255, 255, 255, 0.55);
  text-decoration: none;
  font-size: 13px;
  transition: color var(--transition);
}
.footer-links a:hover {
  color: var(--accent);
}

/* Bottom bar */
.footer-bottom {
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  padding: 20px 24px;
  text-align: center;
}
.footer-bottom p {
  margin: 0;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.35);
}

@media (max-width: 768px) {
  .footer-inner {
    grid-template-columns: 1fr 1fr;
    gap: 32px;
  }
  .brand-col {
    grid-column: 1 / -1;
  }
}

@media (max-width: 480px) {
  .footer-inner {
    grid-template-columns: 1fr;
  }
  .brand-col {
    grid-column: 1;
  }
}
</style>
