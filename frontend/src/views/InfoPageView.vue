<template>
  <div class="info-page">
    <div class="page-hero">
      <h1>{{ pageTitle }}</h1>
      <p>{{ pageSubtitle }}</p>
    </div>

    <div class="content">
      <!-- 配送说明 -->
      <template v-if="page === 'shipping'">
        <section class="info-section">
          <h3>配送范围</h3>
          <p>全国配送（含港澳台），海外地区请联系客服确认。</p>
          <h3>配送时效</h3>
          <el-table :data="shippingData" border stripe>
            <el-table-column prop="region" label="配送区域" />
            <el-table-column prop="method" label="配送方式" />
            <el-table-column prop="time" label="预计时效" />
            <el-table-column prop="fee" label="运费" />
          </el-table>
          <h3>注意事项</h3>
          <ul>
            <li>订单满 299 元享全国包邮</li>
            <li>工作日 16:00 前下单当日发出</li>
            <li>法定节假日顺延至下一工作日</li>
            <li>签收时请当面验货，如有破损可拒收</li>
          </ul>
        </section>
      </template>

      <!-- 退换政策 -->
      <template v-else-if="page === 'returns'">
        <section class="info-section">
          <h3>退换货规则</h3>
          <div class="policy-card">
            <h4>7 天无理由退换</h4>
            <p>自签收之日起 7 日内，商品未经使用、洗涤、损坏，且吊牌完好，可申请无理由退换。运费由买家承担。</p>
          </div>
          <div class="policy-card">
            <h4>质量问题退换</h4>
            <p>若商品存在质量问题（如开线、色差严重、尺寸不符描述），请在签收 48 小时内联系客服，我们承担往返运费。</p>
          </div>
          <h3>不可退换商品</h3>
          <ul>
            <li>贴身衣物（内衣、袜子、泳装等）</li>
            <li>已使用或已洗涤的商品</li>
            <li>定制类及特殊标注商品</li>
            <li>赠品及积分兑换商品</li>
          </ul>
          <h3>退款流程</h3>
          <div class="flow-steps">
            <div class="step"><span class="step-num">1</span>提交申请</div>
            <div class="step-arrow">→</div>
            <div class="step"><span class="step-num">2</span>客服审核</div>
            <div class="step-arrow">→</div>
            <div class="step"><span class="step-num">3</span>寄回商品</div>
            <div class="step-arrow">→</div>
            <div class="step"><span class="step-num">4</span>验收入库</div>
            <div class="step-arrow">→</div>
            <div class="step"><span class="step-num">5</span>退款到账</div>
          </div>
          <p class="hint">退款将在验收后 3-5 个工作日内原路返回至您的支付账户。</p>
        </section>
      </template>

      <!-- 支付方式 -->
      <template v-else-if="page === 'payment'">
        <section class="info-section">
          <h3>支持支付方式</h3>
          <div class="payment-grid">
            <div class="payment-card">
              <div class="pay-icon">💳</div>
              <h4>银行卡</h4>
              <p>支持所有主流银行借记卡与信用卡</p>
            </div>
            <div class="payment-card">
              <div class="pay-icon">📱</div>
              <h4>微信支付</h4>
              <p>扫码支付，即时到账</p>
            </div>
            <div class="payment-card">
              <div class="pay-icon">📲</div>
              <h4>支付宝</h4>
              <p>支持花呗分期付款</p>
            </div>
            <div class="payment-card">
              <div class="pay-icon">🏦</div>
              <h4>银行转账</h4>
              <p>对公账户转账（企业采购适用）</p>
            </div>
          </div>
          <h3>安全保证</h3>
          <p>所有在线支付均通过 PCI-DSS 认证的支付网关处理，LUMINA 不存储您的银行卡信息。交易全程 TLS 加密。</p>
        </section>
      </template>

      <!-- 线下门店 -->
      <template v-else-if="page === 'stores'">
        <section class="info-section">
          <div v-for="store in stores" :key="store.name" class="store-card">
            <div class="store-img">
              <div class="store-placeholder">{{ store.name.charAt(0) }}</div>
            </div>
            <div class="store-info">
              <h4>{{ store.name }}</h4>
              <p class="store-addr">{{ store.address }}</p>
              <p class="store-hours">{{ store.hours }}</p>
              <p class="store-tel">{{ store.tel }}</p>
            </div>
          </div>
        </section>
      </template>

      <!-- 加入我们 -->
      <template v-else-if="page === 'careers'">
        <section class="info-section">
          <h3>开放职位</h3>
          <div v-for="job in jobs" :key="job.title" class="job-card">
            <div class="job-header">
              <h4>{{ job.title }}</h4>
              <span class="job-tag">{{ job.type }}</span>
            </div>
            <p class="job-loc">{{ job.location }} · {{ job.dept }}</p>
            <p class="job-desc">{{ job.desc }}</p>
          </div>
          <div class="careers-contact">
            <p>投递简历至：<strong>hr@lumina.com</strong></p>
            <p class="hint">邮件标题格式：应聘职位 + 姓名</p>
          </div>
        </section>
      </template>

      <!-- 常见问题 -->
      <template v-else-if="page === 'faq'">
        <section class="info-section">
          <el-collapse accordion>
            <el-collapse-item v-for="faq in faqs" :key="faq.q" :title="faq.q">
              <p>{{ faq.a }}</p>
            </el-collapse-item>
          </el-collapse>
        </section>
      </template>

      <!-- 联系我们 -->
      <template v-else-if="page === 'contact'">
        <section class="info-section">
          <div class="contact-grid">
            <div class="contact-card">
              <div class="contact-icon">📧</div>
              <h4>邮箱</h4>
              <p>service@lumina.com</p>
            </div>
            <div class="contact-card">
              <div class="contact-icon">📞</div>
              <h4>电话</h4>
              <p>400-888-6688</p>
            </div>
            <div class="contact-card">
              <div class="contact-icon">💬</div>
              <h4>在线客服</h4>
              <p>工作日 9:00 - 21:00</p>
            </div>
            <div class="contact-card">
              <div class="contact-icon">🏢</div>
              <h4>公司地址</h4>
              <p>上海市静安区南京西路 1515 号</p>
            </div>
          </div>
        </section>
      </template>

      <!-- 隐私政策 -->
      <template v-else-if="page === 'privacy'">
        <section class="info-section legal-text">
          <h3>隐私政策</h3>
          <p><strong>最后更新日期：2026 年 1 月 1 日</strong></p>
          <h4>信息收集</h4>
          <p>我们在您注册账户、下单购买、订阅资讯时收集必要的个人信息，包括但不限于：姓名、手机号码、电子邮箱、收货地址。</p>
          <h4>信息使用</h4>
          <p>收集的信息仅用于：订单处理与物流配送、客户服务与售后支持、个性化推荐与营销通知（可退订）、服务改进与数据分析。</p>
          <h4>信息保护</h4>
          <p>我们采用业界通行的安全技术（SSL/TLS 加密、访问控制、数据脱敏）保护您的个人信息，并定期进行安全审计。</p>
          <h4>信息共享</h4>
          <p>除物流配送所必需的第三方（快递公司）外，我们不会向任何第三方出售、交易或转让您的个人信息。</p>
          <h4>Cookie 政策</h4>
          <p>本站使用 Cookie 改善用户体验。您可以通过浏览器设置禁用 Cookie，但可能影响部分功能使用。</p>
          <h4>您的权利</h4>
          <p>您有权随时查询、更正、删除您的个人信息，或撤回已授予的同意。请通过客服联系我们行使上述权利。</p>
        </section>
      </template>

      <!-- 使用条款 -->
      <template v-else-if="page === 'terms'">
        <section class="info-section legal-text">
          <h3>使用条款</h3>
          <p><strong>最后更新日期：2026 年 1 月 1 日</strong></p>
          <h4>接受条款</h4>
          <p>访问或使用 LUMINA 网站及服务，即表示您同意遵守本使用条款。如不同意，请停止使用。</p>
          <h4>账户注册</h4>
          <p>您需提供真实、准确的注册信息，并对账户安全负责。发现未经授权使用账户的情况，请立即通知我们。</p>
          <h4>订单与价格</h4>
          <p>所有商品价格以结算时页面显示为准。LUMINA 保留在发现明显定价错误时取消订单的权利，届时将全额退款。</p>
          <h4>知识产权</h4>
          <p>本网站所有内容（包括但不限于文字、图片、Logo、设计）均受版权及商标法保护，未经许可不得复制或使用。</p>
          <h4>用户行为</h4>
          <p>您同意不从事以下行为：发布违法或侵权内容、干扰网站正常运行、利用网站进行欺诈活动、未经授权收集他人信息。</p>
          <h4>免责声明</h4>
          <p>LUMINA 按「现状」提供服务，不对商品适销性或特定用途适用性作任何明示或默示保证。在法律允许范围内，我们不对间接损失承担责任。</p>
          <h4>法律适用</h4>
          <p>本条款受中华人民共和国法律管辖。因本条款产生的争议，双方应友好协商；协商不成的，提交有管辖权的人民法院裁决。</p>
        </section>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const page = computed(() => route.meta.page as string)

const pageMeta: Record<string, { title: string; subtitle: string }> = {
  shipping: { title: '配送说明', subtitle: '了解我们的配送服务与时效' },
  returns: { title: '退换政策', subtitle: '7 天无理由退换，让您无忧购物' },
  payment: { title: '支付方式', subtitle: '多种安全支付，便捷可靠' },
  stores: { title: '线下门店', subtitle: '欢迎莅临体验' },
  careers: { title: '加入我们', subtitle: '与 LUMINA 一起成长' },
  faq: { title: '常见问题', subtitle: '快速找到您需要的答案' },
  contact: { title: '联系我们', subtitle: '我们随时为您服务' },
  privacy: { title: '隐私政策', subtitle: '我们如何保护您的个人信息' },
  terms: { title: '使用条款', subtitle: '使用 LUMINA 服务前请仔细阅读' },
}

const pageTitle = computed(() => pageMeta[page.value]?.title || '')
const pageSubtitle = computed(() => pageMeta[page.value]?.subtitle || '')

const shippingData = [
  { region: '一线城市', method: '顺丰速运', time: '1-2 个工作日', fee: '满299包邮 / 不满¥12' },
  { region: '省会城市', method: '顺丰速运', time: '2-3 个工作日', fee: '满299包邮 / 不满¥12' },
  { region: '其他城市', method: '中通快递', time: '3-5 个工作日', fee: '满299包邮 / 不满¥10' },
  { region: '港澳台地区', method: '顺丰特惠', time: '5-7 个工作日', fee: '按实际计费' },
]

const stores = [
  { name: '上海旗舰店', address: '上海市静安区南京西路 1515 号 L1-03', hours: '10:00 - 22:00', tel: '021-6288-6688' },
  { name: '北京三里屯店', address: '北京市朝阳区三里屯太古里南区 S2-11', hours: '10:00 - 22:00', tel: '010-8456-1234' },
  { name: '成都太古里店', address: '成都市锦江区中纱帽街 8 号 L2-18', hours: '10:00 - 22:00', tel: '028-8666-5678' },
  { name: '深圳万象天地店', address: '深圳市南山区深南大道 9668 号 B1-05', hours: '10:00 - 22:00', tel: '0755-8678-9012' },
]

const jobs = [
  { title: '前端开发工程师', type: '全职', location: '上海', dept: '技术部', desc: '负责 LUMINA 电商平台前端开发，熟练使用 Vue 3、TypeScript 及 Element Plus。' },
  { title: '视觉设计师', type: '全职', location: '上海', dept: '设计部', desc: '负责品牌视觉、活动页面及社交媒体内容设计，需具备优秀的审美与排版能力。' },
  { title: '买手助理', type: '全职', location: '上海', dept: '商品部', desc: '协助买手进行市场调研、供应商沟通及样品管理，热爱时尚并有敏锐的市场洞察力。' },
  { title: '客户服务专员', type: '实习', location: '上海', dept: '客服部', desc: '处理客户咨询与售后问题，要求耐心细致，有良好的沟通能力。' },
]

const faqs = [
  { q: '如何修改订单信息？', a: '订单未发货前，您可在「我的账户-订单详情」中修改收货地址。如需修改商品，请取消订单后重新下单。' },
  { q: '如何查询物流信息？', a: '点击「我的账户-我的订单」进入订单详情，即可查看实时物流追踪信息。' },
  { q: '优惠券如何使用？', a: '结算页面会自动展示可用优惠券，选择即可抵扣。每笔订单限用一张优惠券，不可与其他优惠叠加。' },
  { q: '商品尺码如何选择？', a: '请参考商品详情页的尺码推荐，或访问「尺码指南」页面查看详细尺码对照表。' },
  { q: '如何成为 LUMINA 会员？', a: '注册即成为普通会员。累计消费满 2000 元升级银卡会员，满 5000 元升级金卡会员，享受更多专属权益。' },
  { q: '企业采购如何操作？', a: '请发送采购需求至 corporate@lumina.com，我们的企业服务团队将在 1 个工作日内与您联系。' },
]
</script>

<style scoped>
.info-page {
  min-height: 100vh;
  background: var(--bg);
}

.page-hero {
  background: linear-gradient(135deg, #f5f0e8, #ede4d3);
  text-align: center;
  padding: 64px 24px 48px;
}
.page-hero h1 {
  font-size: 32px;
  font-weight: 300;
  letter-spacing: 2px;
  margin: 0 0 8px;
  font-family: var(--font-serif, serif);
}
.page-hero p {
  color: var(--text-muted);
  font-size: 14px;
  margin: 0;
}

.content {
  max-width: 800px;
  margin: 0 auto;
  padding: 48px 24px 80px;
}

.info-section h3 {
  font-size: 20px;
  font-weight: 600;
  margin: 32px 0 16px;
}
.info-section h3:first-child {
  margin-top: 0;
}

.info-section h4 {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 8px;
}

.info-section p {
  font-size: 15px;
  line-height: 2;
  color: var(--text-muted);
  margin: 0 0 16px;
}

.info-section ul {
  padding-left: 20px;
  margin: 0 0 16px;
}
.info-section ul li {
  font-size: 14px;
  line-height: 2;
  color: var(--text-muted);
}

.hint {
  font-size: 13px !important;
  color: var(--text-muted) !important;
  opacity: 0.7;
}

/* Policy cards */
.policy-card {
  background: #fff;
  border: 1px solid var(--border);
  border-radius: var(--radius);
  padding: 20px 24px;
  margin-bottom: 12px;
}

/* Return flow */
.flow-steps {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}
.step {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #fff;
  border: 1px solid var(--border);
  border-radius: var(--radius);
  padding: 10px 16px;
  font-size: 13px;
  color: var(--text);
}
.step-num {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: var(--primary);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
}
.step-arrow {
  color: var(--border);
  font-size: 16px;
}

/* Payment */
.payment-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 32px;
}
.payment-card {
  text-align: center;
  padding: 24px 12px;
  background: #fff;
  border: 1px solid var(--border);
  border-radius: var(--radius);
}
.pay-icon {
  font-size: 32px;
  margin-bottom: 12px;
}
.payment-card h4 {
  margin: 0 0 8px;
}
.payment-card p {
  font-size: 12px;
  margin: 0;
}

/* Stores */
.store-card {
  display: flex;
  gap: 20px;
  padding: 20px;
  background: #fff;
  border: 1px solid var(--border);
  border-radius: var(--radius);
  margin-bottom: 16px;
}
.store-img {
  flex-shrink: 0;
  width: 100px;
  height: 80px;
}
.store-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #f5f0e8, #ede4d3);
  border-radius: var(--radius-sm, 6px);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: var(--accent);
  font-family: var(--font-serif, serif);
}
.store-info h4 {
  margin: 0 0 6px;
  font-size: 16px;
}
.store-addr, .store-hours, .store-tel {
  font-size: 13px !important;
  margin: 0 0 4px !important;
  line-height: 1.6 !important;
}

/* Jobs */
.job-card {
  border: 1px solid var(--border);
  border-radius: var(--radius);
  padding: 20px 24px;
  margin-bottom: 12px;
  background: #fff;
}
.job-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}
.job-header h4 {
  margin: 0;
  font-size: 16px;
}
.job-tag {
  font-size: 11px;
  padding: 2px 10px;
  border-radius: 100px;
  background: #f5f0e8;
  color: var(--accent);
  font-weight: 600;
}
.job-loc {
  font-size: 13px !important;
  margin: 0 0 8px !important;
}
.job-desc {
  font-size: 14px !important;
  margin: 0 !important;
}
.careers-contact {
  margin-top: 32px;
  padding: 24px;
  background: #fafaf8;
  border-radius: var(--radius);
  text-align: center;
}

/* Contact */
.contact-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 40px;
}
.contact-card {
  text-align: center;
  padding: 24px 12px;
  background: #fff;
  border: 1px solid var(--border);
  border-radius: var(--radius);
}
.contact-icon {
  font-size: 28px;
  margin-bottom: 8px;
}
.contact-card h4 {
  font-size: 15px;
  margin: 0 0 4px;
}
.contact-card p {
  font-size: 13px;
  margin: 0;
}
/* Legal text */
.legal-text h4 {
  margin-top: 24px;
  font-size: 16px;
}
.legal-text p {
  font-size: 14px;
}

/* Collapse override */
:deep(.el-collapse-item__header) {
  font-size: 15px;
  font-weight: 500;
  height: 52px;
}

@media (max-width: 768px) {
  .payment-grid, .contact-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .flow-steps {
    justify-content: center;
  }
  .store-card {
    flex-direction: column;
  }
}
</style>
