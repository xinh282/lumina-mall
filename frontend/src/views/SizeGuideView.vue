<template>
  <div class="size-guide-page">
    <div class="page-hero">
      <h1>尺码指南</h1>
      <p>找到最适合你的完美尺码</p>
    </div>

    <div class="content">
      <!-- 女装尺码 -->
      <section class="size-section">
        <h2>女装尺码对照表</h2>
        <p class="section-desc">根据身高体重选择合适尺码，建议按实际体型参考</p>
        <el-table :data="womenSizes" border stripe style="width: 100%">
          <el-table-column prop="size" label="尺码" width="100" align="center" />
          <el-table-column prop="cn" label="中国码" width="120" align="center" />
          <el-table-column prop="height" label="身高(cm)" width="140" align="center" />
          <el-table-column prop="weight" label="体重(kg)" width="140" align="center" />
          <el-table-column prop="bust" label="胸围(cm)" width="120" align="center" />
          <el-table-column prop="waist" label="腰围(cm)" width="120" align="center" />
          <el-table-column prop="hip" label="臀围(cm)" width="120" align="center" />
        </el-table>
      </section>

      <!-- 男装尺码 -->
      <section class="size-section">
        <h2>男装尺码对照表</h2>
        <p class="section-desc">亚洲版型，建议按身高优先选择</p>
        <el-table :data="menSizes" border stripe style="width: 100%">
          <el-table-column prop="size" label="尺码" width="100" align="center" />
          <el-table-column prop="cn" label="中国码" width="120" align="center" />
          <el-table-column prop="height" label="身高(cm)" width="160" align="center" />
          <el-table-column prop="weight" label="体重(kg)" width="160" align="center" />
          <el-table-column prop="chest" label="胸围(cm)" width="140" align="center" />
          <el-table-column prop="shoulder" label="肩宽(cm)" width="140" align="center" />
        </el-table>
      </section>

      <!-- 鞋码 -->
      <section class="size-section">
        <h2>鞋码对照表</h2>
        <div class="shoe-grid">
          <div class="shoe-col">
            <h3>女鞋</h3>
            <el-table :data="womenShoes" border stripe>
              <el-table-column prop="cn" label="中国码" align="center" />
              <el-table-column prop="eu" label="欧洲码" align="center" />
              <el-table-column prop="us" label="美国码" align="center" />
              <el-table-column prop="foot" label="脚长(cm)" align="center" />
            </el-table>
          </div>
          <div class="shoe-col">
            <h3>男鞋</h3>
            <el-table :data="menShoes" border stripe>
              <el-table-column prop="cn" label="中国码" align="center" />
              <el-table-column prop="eu" label="欧洲码" align="center" />
              <el-table-column prop="us" label="美国码" align="center" />
              <el-table-column prop="foot" label="脚长(cm)" align="center" />
            </el-table>
          </div>
        </div>
      </section>

      <!-- 测量方法 -->
      <section class="size-section">
        <h2>如何正确测量</h2>
        <div class="measure-grid">
          <div class="measure-card">
            <div class="measure-icon">📏</div>
            <h4>胸围</h4>
            <p>绕胸部最丰满处水平一周，保持正常呼吸</p>
          </div>
          <div class="measure-card">
            <div class="measure-icon">📐</div>
            <h4>腰围</h4>
            <p>绕腰部最细处水平一周，自然站立放松</p>
          </div>
          <div class="measure-card">
            <div class="measure-icon">📋</div>
            <h4>臀围</h4>
            <p>绕臀部最丰满处水平一周，双脚并拢</p>
          </div>
          <div class="measure-card">
            <div class="measure-icon">📝</div>
            <h4>肩宽</h4>
            <p>从左肩端点经后颈到右肩端点的弧线距离</p>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
const womenSizes = [
  { size: 'XS', cn: '155/80A', height: '150-155', weight: '40-45', bust: '78-82', waist: '60-64', hip: '84-88' },
  { size: 'S', cn: '160/84A', height: '155-160', weight: '45-50', bust: '82-86', waist: '64-68', hip: '88-92' },
  { size: 'M', cn: '165/88A', height: '160-165', weight: '50-57', bust: '86-90', waist: '68-72', hip: '92-96' },
  { size: 'L', cn: '170/92A', height: '165-170', weight: '57-64', bust: '90-94', waist: '72-76', hip: '96-100' },
  { size: 'XL', cn: '175/96A', height: '170-175', weight: '64-72', bust: '94-98', waist: '76-80', hip: '100-104' },
]

const menSizes = [
  { size: 'S', cn: '165/88A', height: '160-168', weight: '50-60', chest: '88-92', shoulder: '42-44' },
  { size: 'M', cn: '170/92A', height: '168-175', weight: '60-70', chest: '92-96', shoulder: '44-46' },
  { size: 'L', cn: '175/96A', height: '175-182', weight: '70-80', chest: '96-100', shoulder: '46-48' },
  { size: 'XL', cn: '180/100A', height: '182-188', weight: '80-90', chest: '100-104', shoulder: '48-50' },
  { size: 'XXL', cn: '185/104A', height: '188-195', weight: '90-100', chest: '104-108', shoulder: '50-52' },
]

const womenShoes = [
  { cn: '35', eu: '36', us: '5.5', foot: '22.5' },
  { cn: '36', eu: '37', us: '6', foot: '23.0' },
  { cn: '37', eu: '38', us: '7', foot: '23.5' },
  { cn: '38', eu: '39', us: '7.5', foot: '24.0' },
  { cn: '39', eu: '40', us: '8.5', foot: '25.0' },
  { cn: '40', eu: '41', us: '9', foot: '25.5' },
]

const menShoes = [
  { cn: '39', eu: '40', us: '7', foot: '24.5' },
  { cn: '40', eu: '41', us: '8', foot: '25.0' },
  { cn: '41', eu: '42', us: '8.5', foot: '26.0' },
  { cn: '42', eu: '43', us: '9.5', foot: '26.5' },
  { cn: '43', eu: '44', us: '10', foot: '27.5' },
  { cn: '44', eu: '45', us: '11', foot: '28.0' },
  { cn: '45', eu: '46', us: '12', foot: '29.0' },
]
</script>

<style scoped>
.size-guide-page {
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
  font-weight: 700;
  letter-spacing: 2px;
  margin: 0 0 8px;
}
.page-hero p {
  color: var(--text-muted);
  font-size: 14px;
  margin: 0;
}
.content {
  max-width: 960px;
  margin: 0 auto;
  padding: 48px 24px;
}
.size-section {
  margin-bottom: 56px;
}
.size-section h2 {
  font-size: 22px;
  font-weight: 700;
  margin: 0 0 4px;
}
.section-desc {
  font-size: 13px;
  color: var(--text-muted);
  margin: 0 0 20px;
}
.shoe-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 32px;
}
.shoe-col h3 {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 12px;
}
.measure-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}
.measure-card {
  text-align: center;
  padding: 24px 16px;
  background: #fff;
  border-radius: 12px;
  border: 1px solid var(--border);
}
.measure-icon {
  font-size: 28px;
  margin-bottom: 8px;
}
.measure-card h4 {
  font-size: 15px;
  font-weight: 600;
  margin: 0 0 6px;
}
.measure-card p {
  font-size: 13px;
  color: var(--text-muted);
  margin: 0;
  line-height: 1.6;
}
@media (max-width: 768px) {
  .shoe-grid { grid-template-columns: 1fr; }
  .measure-grid { grid-template-columns: repeat(2, 1fr); }
  .page-hero h1 { font-size: 26px; }
}
</style>
