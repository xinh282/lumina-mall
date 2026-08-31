<template>
  <div class="big-screen">
    <!-- 顶部标题栏 -->
    <div class="screen-header">
      <div class="header-left">
        <span class="deco-line"></span>
        <span class="header-title">LUMINA 数据可视化大屏</span>
        <span class="deco-line"></span>
      </div>
      <div class="header-right">
        <span class="header-time">{{ nowStr }}</span>
        <el-button size="small" type="warning" plain @click="exportExcel">导出报表</el-button>
      </div>
    </div>

    <!-- 6 指标卡片 -->
    <div class="card-row">
      <div v-for="c in cards" :key="c.label" class="card-item">
        <div class="card-label">{{ c.label }}</div>
        <div class="card-value">
          <span v-if="c.unit === '¥'" class="card-unit">¥</span>{{ c.value }}
          <span v-if="c.unit === '单'" class="card-unit">单</span>
        </div>
        <div class="card-trend" :class="trendClass(c.trend)">
          <span class="trend-arrow">{{ c.trend > 0 ? '↑' : c.trend < 0 ? '↓' : '→' }}</span>
          {{ c.trend != null ? Math.abs(c.trend) + '%' : '--' }}
          <span class="trend-label">较上期</span>
        </div>
      </div>
    </div>

    <!-- 折线柱状混合图 -->
    <div class="chart-row">
      <div class="chart-box">
        <div class="box-corner tl"></div><div class="box-corner tr"></div>
        <div class="box-title">月度订单趋势</div>
        <div ref="orderChart" class="chart-body"></div>
      </div>
      <div class="chart-box">
        <div class="box-corner tl"></div><div class="box-corner tr"></div>
        <div class="box-title">月度成交额趋势</div>
        <div ref="amountChart" class="chart-body"></div>
      </div>
    </div>

    <!-- 饼图 + 排行 -->
    <div class="bottom-row">
      <div class="chart-box half">
        <div class="box-corner tl"></div><div class="box-corner tr"></div>
        <div class="box-title">TOP5 销售额占比</div>
        <div ref="pieChart" class="chart-body"></div>
      </div>
      <div class="chart-box half">
        <div class="box-corner tl"></div><div class="box-corner tr"></div>
        <div class="box-title">商品销量 TOP5</div>
        <div class="rank-table">
          <div v-for="(p, i) in topProducts" :key="i" class="rank-row">
            <span class="rank-num" :class="{ top3: i < 3 }">{{ i + 1 }}</span>
            <span class="rank-name">{{ p.productName }}</span>
            <div class="rank-bar-wrap">
              <div class="rank-bar" :style="{ width: barWidth(p, i) + '%' }"></div>
            </div>
            <span class="rank-val">{{ p.totalQuantity }}单</span>
            <span class="rank-amount">¥{{ p.totalRevenue }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import request from '@/api/request'

const nowStr = ref('')
let timer: ReturnType<typeof setInterval> | undefined

const cards = ref<any[]>([])
const topProducts = ref<any[]>([])
let orderChart: echarts.ECharts | null = null
let amountChart: echarts.ECharts | null = null
let pieChart: echarts.ECharts | null = null

const orderChartRef = ref<HTMLElement>()
const amountChartRef = ref<HTMLElement>()
const pieChartRef = ref<HTMLElement>()

function trendClass(v: number | null) {
  if (v == null) return 'flat'
  return v > 0 ? 'up' : v < 0 ? 'down' : 'flat'
}
function barWidth(p: any, _i: number) {
  const max = topProducts.value[0]?.totalQuantity || 1
  return Math.round((p.totalQuantity / max) * 100)
}

async function fetchData() {
  try {
    const res = await request.get<any, any>('/shop/getShopBigScreenData')
    const d = res.data ?? res
    cards.value = d.cards || []
    topProducts.value = d.topProducts || []
    renderOrderChart(d.trendDates || [], d.trendOrders || [], d.trendAmounts || [])
    renderAmountChart(d.trendDates || [], d.trendAmounts || [])
    renderPieChart(d.pieData || [])
  } catch { /* */ }
}

function renderOrderChart(dates: string[], orders: number[], amounts: number[]) {
  if (!orderChartRef.value) return
  if (!orderChart) orderChart = echarts.init(orderChartRef.value)
  orderChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['订单数', '成交额'], textStyle: { color: '#8899aa' }, top: 0 },
    grid: { top: 36, right: 24, bottom: 36, left: 48 },
    xAxis: { type: 'category', data: dates, axisLabel: { color: '#8899aa', rotate: 30, fontSize: 10 } },
    yAxis: [
      { type: 'value', name: '单', axisLabel: { color: '#8899aa' }, splitLine: { lineStyle: { color: '#1a2a3a' } } },
      { type: 'value', name: '¥', axisLabel: { color: '#8899aa' }, splitLine: { show: false } },
    ],
    series: [
      { name: '订单数', type: 'bar', data: orders, itemStyle: { color: '#00d4ff' }, barWidth: 14 },
      { name: '成交额', type: 'line', yAxisIndex: 1, data: amounts, itemStyle: { color: '#f5a623' }, smooth: true },
    ],
  }, true)
}

function renderAmountChart(dates: string[], amounts: number[]) {
  if (!amountChartRef.value) return
  if (!amountChart) amountChart = echarts.init(amountChartRef.value)
  amountChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { top: 16, right: 24, bottom: 36, left: 56 },
    xAxis: { type: 'category', data: dates, axisLabel: { color: '#8899aa', rotate: 30, fontSize: 10 } },
    yAxis: { type: 'value', name: '¥', axisLabel: { color: '#8899aa' }, splitLine: { lineStyle: { color: '#1a2a3a' } } },
    series: [{
      name: '成交额', type: 'bar', data: amounts,
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#00d4ff' }, { offset: 1, color: '#003355' },
        ]),
      }, barWidth: 16,
    }],
  }, true)
}

function renderPieChart(data: any[]) {
  if (!pieChartRef.value) return
  if (!pieChart) pieChart = echarts.init(pieChartRef.value)
  pieChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { orient: 'vertical', right: 10, top: 'center', textStyle: { color: '#8899aa' } },
    series: [{
      type: 'pie', radius: ['45%', '72%'], center: ['40%', '50%'],
      label: { color: '#8899aa', formatter: '{b}\n{d}%' },
      data,
      itemStyle: { borderColor: '#0a1628', borderWidth: 3 },
    }],
  }, true)
}

function exportExcel() {
  request.get('/admin/dashboard/export', { responseType: 'blob' }).then(res => {
    const url = window.URL.createObjectURL(new Blob([res as any]))
    const a = document.createElement('a'); a.href = url
    a.download = 'LUMINA报表.xlsx'; a.click(); window.URL.revokeObjectURL(url)
  }).catch(() => {})
}

function updateTime() {
  const d = new Date()
  nowStr.value = d.getFullYear() + '-' +
    String(d.getMonth() + 1).padStart(2, '0') + '-' +
    String(d.getDate()).padStart(2, '0') + ' ' +
    String(d.getHours()).padStart(2, '0') + ':' +
    String(d.getMinutes()).padStart(2, '0') + ':' +
    String(d.getSeconds()).padStart(2, '0')
}

onMounted(async () => {
  updateTime()
  timer = setInterval(updateTime, 1000)
  await fetchData()
  setInterval(fetchData, 60000)
  window.addEventListener('resize', () => {
    orderChart?.resize(); amountChart?.resize(); pieChart?.resize()
  })
})
onUnmounted(() => {
  clearInterval(timer)
  orderChart?.dispose(); amountChart?.dispose(); pieChart?.dispose()
})
</script>

<style scoped>
.big-screen {
  min-height: 100vh;
  background: linear-gradient(180deg, #0a1628 0%, #0d1f3c 50%, #0a1628 100%);
  color: #e0e6f0;
  padding: 10px 18px 20px;
  font-family: 'Microsoft YaHei', sans-serif;
}

.screen-header {
  display: flex; align-items: center; justify-content: center;
  height: 48px; position: relative; margin-bottom: 10px;
}
.header-left { display: flex; align-items: center; gap: 16px; }
.deco-line {
  width: 100px; height: 2px;
  background: linear-gradient(90deg, transparent, #00d4ff, transparent);
}
.header-title {
  font-size: 22px; font-weight: 700; letter-spacing: 3px;
  background: linear-gradient(90deg, #00d4ff, #5be0ff);
  -webkit-background-clip: text; -webkit-text-fill-color: transparent;
}
.header-right { position: absolute; right: 8px; display: flex; align-items: center; gap: 14px; }
.header-time { font-size: 14px; color: #00d4ff; font-family: monospace; }

.card-row {
  display: grid; grid-template-columns: repeat(6, 1fr); gap: 10px; margin-bottom: 10px;
}
.card-item {
  background: linear-gradient(135deg, rgba(0,212,255,0.10), rgba(0,100,160,0.06));
  border: 1px solid rgba(0,212,255,0.18); border-radius: 6px;
  padding: 12px 14px; text-align: center;
  position: relative; overflow: hidden;
}
.card-item::before {
  content: ''; position: absolute; top: 0; left: 0; right: 0; height: 2px;
  background: linear-gradient(90deg, transparent, rgba(0,212,255,0.6), transparent);
}
.card-label { font-size: 12px; color: #8899aa; margin-bottom: 4px; }
.card-value { font-size: 24px; font-weight: 700; color: #fff; }
.card-unit { font-size: 13px; color: #8899aa; margin: 0 3px; }
.card-trend { font-size: 11px; margin-top: 3px; }
.card-trend.up { color: #f5a623; }
.card-trend.down { color: #4fc08d; }
.card-trend.flat { color: #8899aa; }
.trend-arrow { margin-right: 2px; }
.trend-label { color: #445566; margin-left: 4px; }

.chart-row {
  display: grid; grid-template-columns: 1fr 1fr; gap: 10px; margin-bottom: 10px;
}
.bottom-row { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; }

.chart-box {
  background: rgba(10,22,40,0.9); border: 1px solid rgba(0,212,255,0.12);
  border-radius: 4px; padding: 6px; position: relative;
}
.box-corner { position: absolute; width: 8px; height: 8px; border-color: #00d4ff; border-style: solid; }
.box-corner.tl { top: 0; left: 0; border-width: 2px 0 0 2px; }
.box-corner.tr { top: 0; right: 0; border-width: 2px 2px 0 0; }
.box-title {
  font-size: 13px; font-weight: 600; color: #bcc9d4; padding: 4px 10px 6px;
  border-bottom: 1px solid rgba(0,212,255,0.08); margin-bottom: 2px;
}
.chart-body { height: 260px; }

.rank-table { padding: 8px 10px; }
.rank-row {
  display: flex; align-items: center; gap: 8px;
  padding: 9px 0; border-bottom: 1px solid rgba(255,255,255,0.03);
}
.rank-num {
  width: 22px; height: 22px; border-radius: 4px;
  display: flex; align-items: center; justify-content: center;
  font-size: 12px; font-weight: 700; background: rgba(255,255,255,0.05); color: #8899aa;
  flex-shrink: 0;
}
.rank-num.top3 { background: linear-gradient(135deg, #f5a623, #d4891a); color: #fff; }
.rank-name { width: 90px; font-size: 12px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; flex-shrink: 0; }
.rank-bar-wrap { flex: 1; height: 6px; background: rgba(255,255,255,0.04); border-radius: 3px; overflow: hidden; }
.rank-bar { height: 100%; background: linear-gradient(90deg, #00d4ff, #0088bb); border-radius: 3px; transition: width 0.6s; }
.rank-val { font-size: 11px; color: #8899aa; width: 36px; text-align: right; flex-shrink: 0; }
.rank-amount { font-size: 12px; font-weight: 600; color: #f5a623; width: 65px; text-align: right; flex-shrink: 0; }

@media (max-width: 1400px) {
  .card-row { grid-template-columns: repeat(3, 1fr); }
  .chart-row, .bottom-row { grid-template-columns: 1fr; }
}
</style>
