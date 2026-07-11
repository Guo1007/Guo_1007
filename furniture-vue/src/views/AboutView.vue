<template>
  <div class="about-page">
    <!-- Hero -->
    <section class="about-hero">
      <div class="about-hero-inner">
        <p class="about-tag">品牌故事</p>
        <h1 class="about-title">用心打造每一件家具</h1>
        <p class="about-sub">WOODSPACE — 让家成为生活的艺术品</p>
      </div>
    </section>

    <!-- Story -->
    <section class="about-section">
      <div class="about-row">
        <div class="about-text">
          <h2>我们的故事</h2>
          <p>
            WOODSPACE 创立于 2018 年，源于一份对家居生活的热爱与执着。
            我们相信，家具不仅是空间中的陈设，更是承载家庭记忆与情感的存在。
          </p>
          <p>
            从最初的三人设计工作室，到如今服务超过 50,000 个家庭，我们始终坚守初心 —
            将自然材质与现代设计完美融合，为每一个家打造可以传承的经典之作。
          </p>
        </div>
        <div class="about-visual">
          <div class="about-img-placeholder">
            <span>🏭</span>
            <p>我们的工厂</p>
          </div>
        </div>
      </div>
    </section>

    <!-- Values -->
    <section class="values-section">
      <div class="values-inner">
        <h2 class="values-title">我们的理念</h2>
        <div class="values-grid">
          <div class="value-card">
            <span class="value-icon">🌳</span>
            <h3>天然选材</h3>
            <p>精选北美黑胡桃木、欧洲白橡木等优质原材，每一块木料都经过严格筛选与自然干燥处理。</p>
          </div>
          <div class="value-card">
            <span class="value-icon">🔨</span>
            <h3>匠心工艺</h3>
            <p>传承传统榫卯结构，结合现代精密加工技术，每一处接合都追求毫厘之间的精确。</p>
          </div>
          <div class="value-card">
            <span class="value-icon">🎨</span>
            <h3>简约设计</h3>
            <p>以"少即是多"为设计哲学，去除繁复装饰，让家具回归本质 — 实用、耐用、美观。</p>
          </div>
          <div class="value-card">
            <span class="value-icon">🌍</span>
            <h3>绿色环保</h3>
            <p>坚持使用环保涂料与可持续材料，打造对家人安全、对环境友好的家居产品。</p>
          </div>
        </div>
      </div>
    </section>

    <!-- Contact -->
    <section class="contact-section">
      <div class="contact-inner">
        <h2>联系我们</h2>
        <p class="contact-sub">如有任何问题或合作意向，欢迎随时与我们联系</p>
        <div class="contact-grid">
          <div class="contact-card">
            <span class="contact-icon">📞</span>
            <h3>客服热线</h3>
            <p>400-888-8888</p>
            <p class="contact-note">周一至周日 9:00 - 21:00</p>
          </div>
          <div class="contact-card">
            <span class="contact-icon">📧</span>
            <h3>电子邮箱</h3>
            <p>service@woodspace.com</p>
            <p class="contact-note">24小时内回复</p>
          </div>
          <div class="contact-card">
            <span class="contact-icon">📍</span>
            <h3>总部地址</h3>
            <p>广东省深圳市南山区</p>
            <p class="contact-note">科技园路 88 号 WOODSPACE 大厦</p>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
</script>

<style scoped>
.about-page { background: var(--color-bg); }

/* Hero */
.about-hero {
  background: var(--color-dark);
  color: #fff;
  text-align: center;
  padding: var(--space-16) var(--space-6);
}
.about-tag {
  font-size: var(--text-xs);
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.15em;
  color: var(--color-accent);
  margin-bottom: var(--space-4);
}
.about-title {
  font-size: 2.5rem;
  font-weight: 700;
  font-family: var(--font-serif);
  margin-bottom: var(--space-4);
}
.about-sub {
  font-size: var(--text-lg);
  color: rgba(255,255,255,0.6);
}

/* Story */
.about-section {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: var(--space-16) var(--space-6);
}
.about-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-12);
  align-items: center;
}
.about-text h2 {
  font-size: var(--text-3xl);
  font-weight: 700;
  font-family: var(--font-serif);
  color: var(--color-text-primary);
  margin-bottom: var(--space-6);
}
.about-text p {
  font-size: var(--text-sm);
  color: var(--color-text-secondary);
  line-height: var(--leading-relaxed);
  margin-bottom: var(--space-4);
}
.about-img-placeholder {
  aspect-ratio: 4 / 3;
  background: var(--color-surface);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-xl);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  font-size: 64px;
}
.about-img-placeholder p {
  font-size: var(--text-sm);
  color: var(--color-text-tertiary);
  margin-top: var(--space-4);
}

/* Values */
.values-section {
  background: var(--color-surface);
  border-top: 1px solid var(--color-border-light);
  border-bottom: 1px solid var(--color-border-light);
}
.values-inner {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: var(--space-12) var(--space-6);
}
.values-title {
  text-align: center;
  font-size: var(--text-2xl);
  font-weight: 700;
  font-family: var(--font-serif);
  color: var(--color-text-primary);
  margin-bottom: var(--space-10);
}
.values-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-6);
}
.value-card { text-align: center; padding: var(--space-6); }
.value-icon { font-size: 40px; display: block; margin-bottom: var(--space-4); }
.value-card h3 {
  font-size: var(--text-md);
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: var(--space-2);
}
.value-card p {
  font-size: var(--text-xs);
  color: var(--color-text-tertiary);
  line-height: var(--leading-relaxed);
}

/* Contact */
.contact-section {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: var(--space-12) var(--space-6);
}
.contact-inner h2 {
  text-align: center;
  font-size: var(--text-2xl);
  font-weight: 700;
  font-family: var(--font-serif);
  color: var(--color-text-primary);
  margin-bottom: var(--space-2);
}
.contact-sub {
  text-align: center;
  font-size: var(--text-sm);
  color: var(--color-text-tertiary);
  margin-bottom: var(--space-10);
}
.contact-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-6);
}
.contact-card {
  text-align: center;
  padding: var(--space-8) var(--space-6);
  background: var(--color-surface);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-lg);
}
.contact-icon { font-size: 36px; display: block; margin-bottom: var(--space-4); }
.contact-card h3 {
  font-size: var(--text-md);
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: var(--space-3);
}
.contact-card p { font-size: var(--text-sm); color: var(--color-text-secondary); }
.contact-note { font-size: var(--text-xs) !important; color: var(--color-text-tertiary) !important; margin-top: var(--space-1); }

@media (max-width: 768px) {
  .about-row { grid-template-columns: 1fr; }
  .values-grid { grid-template-columns: repeat(2, 1fr); }
  .contact-grid { grid-template-columns: 1fr; }
  .about-title { font-size: 2rem; }
}
</style>
