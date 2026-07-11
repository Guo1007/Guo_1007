<template>
  <footer class="app-footer">
    <div class="footer-inner">
      <!-- Brand column -->
      <div class="footer-brand">
        <div class="footer-logo">
          <span class="footer-logo-mark">木</span>
          <span class="footer-logo-name">WOODSPACE</span>
        </div>
        <p class="footer-desc">
          精选优质家居好物，打造理想生活空间。<br/>
          每一件家具，都承载着对生活的热爱。
        </p>
        <div class="footer-contact">
          <div class="contact-item">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"/></svg>
            <span>400-888-8888</span>
          </div>
          <div class="contact-item">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/></svg>
            <span>service@woodspace.com</span>
          </div>
        </div>
      </div>

      <!-- Link columns -->
      <div class="footer-links">
        <div class="link-group">
          <h4>购物指南</h4>
          <router-link to="/type/0">全部商品</router-link>
          <a href="#">新品上市</a>
          <a href="#">热销排行</a>
          <a href="#">限时特惠</a>
        </div>
        <div class="link-group">
          <h4>客户服务</h4>
          <router-link to="/user/orders">我的订单</router-link>
          <router-link to="/user/addresses">收货地址</router-link>
          <a href="#">配送说明</a>
          <a href="#">退换政策</a>
        </div>
        <div class="link-group">
          <h4>关于我们</h4>
          <router-link to="/about">品牌故事</router-link>
          <a href="#">联系我们</a>
          <a href="#">加入我们</a>
          <a href="#">隐私政策</a>
        </div>
      </div>
    </div>

    <div class="footer-bottom">
      <div class="footer-bottom-inner">
        <span>&copy; 2026 WOODSPACE 家具商城. All rights reserved.</span>
        <span class="footer-icp">用心打造每一件家具</span>
      </div>
    </div>
  </footer>
</template>

<script setup>
</script>

<style scoped>
.app-footer {
  background: var(--color-dark);
  color: rgba(255,255,255,0.7);
  margin-top: var(--space-16);
}

.footer-inner {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: var(--space-12) var(--space-6) var(--space-10);
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  gap: var(--space-12);
}

/* Brand */
.footer-logo {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-bottom: var(--space-4);
}
.footer-logo-mark {
  width: 32px;
  height: 32px;
  background: rgba(255,255,255,0.15);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 700;
  border-radius: var(--radius-md);
  font-family: var(--font-serif);
}
.footer-logo-name {
  font-size: var(--text-lg);
  font-weight: 700;
  color: #fff;
  letter-spacing: 0.06em;
}
.footer-desc {
  font-size: var(--text-sm);
  line-height: var(--leading-relaxed);
  color: rgba(255,255,255,0.5);
  margin-bottom: var(--space-5);
}
.footer-contact {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}
.contact-item {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--text-sm);
  color: rgba(255,255,255,0.6);
}
.contact-item svg { flex-shrink: 0; opacity: 0.6; }

/* Links */
.footer-links {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-8);
}
.link-group h4 {
  font-size: var(--text-sm);
  font-weight: 600;
  color: #fff;
  margin-bottom: var(--space-4);
}
.link-group a,
.link-group .router-link-exact-active {
  display: block;
  font-size: var(--text-sm);
  color: rgba(255,255,255,0.5);
  text-decoration: none;
  padding: var(--space-1) 0;
  transition: color var(--transition-fast);
}
.link-group a:hover { color: rgba(255,255,255,0.8); }

/* Bottom */
.footer-bottom {
  border-top: 1px solid rgba(255,255,255,0.08);
  padding: var(--space-5) var(--space-6);
}
.footer-bottom-inner {
  max-width: var(--max-width);
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  font-size: var(--text-xs);
  color: rgba(255,255,255,0.35);
}
.footer-icp { font-family: var(--font-serif); }

@media (max-width: 768px) {
  .footer-inner {
    grid-template-columns: 1fr;
    gap: var(--space-8);
    padding: var(--space-8) var(--space-4);
  }
  .footer-links { grid-template-columns: repeat(2, 1fr); }
  .footer-bottom-inner { flex-direction: column; gap: var(--space-2); text-align: center; }
}
</style>
