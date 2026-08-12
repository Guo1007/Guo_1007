<template>
  <div class="about-page">
    <!-- Hero -->
    <section class="about-hero">
      <div class="about-hero-inner">
        <p class="about-tag">{{ heroTag }}</p>
        <h1 class="about-title">{{ heroTitle }}</h1>
        <p class="about-sub">{{ heroSub }}</p>
      </div>
    </section>

    <!-- 4 大购物保障 -->
    <section class="values-section">
      <div class="values-inner">
        <h2 class="values-title">{{ valuesTitle }}</h2>
        <div class="values-grid">
          <div class="value-card" v-for="v in platformPromises" :key="v.title">
            <span class="value-icon">{{ v.icon }}</span>
            <h3>{{ v.title }}</h3>
            <p>{{ v.text }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- 客服与反馈 -->
    <section class="contact-section" id="contact">
      <div class="contact-inner">
        <h2>{{ contactTitle }}</h2>
        <p class="contact-sub">{{ contactSub }}</p>
        <div class="contact-grid">
          <div class="contact-card">
            <span class="contact-icon">📞</span>
            <h3>客服热线</h3>
            <p>{{ sys.contactInfo.phone }}</p>
            <p class="contact-note">{{ contact.phoneNote }}</p>
          </div>
          <div class="contact-card">
            <span class="contact-icon">📧</span>
            <h3>电子邮箱</h3>
            <p>{{ sys.contactInfo.email }}</p>
            <p class="contact-note">{{ contact.emailNote }}</p>
          </div>
          <div class="contact-card">
            <span class="contact-icon">📍</span>
            <h3>总部地址</h3>
            <p>{{ contact.address }}</p>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { useSystemStore } from "@/stores/system.js";
const sys = useSystemStore();

const parseExtra = (str) => {
  try {
    return JSON.parse(str) || {};
  } catch {
    return {};
  }
};

const heroTag = ref("关于平台");
const heroTitle = ref("平台简介");
const heroSub = ref("专注优质家居好物一站式在线购物平台");
const valuesTitle = ref("选择我们，放心购物");
const contactTitle = ref("客服与反馈");
const contactSub = ref("如有任何问题或建议，欢迎随时联系我们");

const platformPromises = ref([
  { icon: "✓", title: "正品保障", text: "官方授权品牌入驻，所有商品假一赔十" },
  { icon: "🚚", title: "极速发货", text: "工作日 48 小时内安排出库发货" },
  { icon: "↩️", title: "7 天无理由", text: "收到货后 7 天内支持无理由退换" },
  { icon: "🤖", title: "AI 智能客服", text: "小智全天候在线提供家具选购建议" },
]);

const contact = ref({
  phone: "",
  email: "",
  address: "",
  phoneNote: "",
  emailNote: "",
});

onMounted(async () => {
  await sys.load();
  try {
    const data = sys.siteData;
    if (!data) return;

    // Labels
    const labels = data.label || [];
    const findLabel = (key) => labels.find((l) => l.sectionKey === key);
    const h = findLabel("about_hero_tag");
    if (h) heroTag.value = h.contentTitle || heroTag.value;
    const hs = findLabel("about_hero_sub");
    if (hs?.contentText) heroSub.value = hs.contentText;
    const vt = findLabel("about_values_title");
    if (vt) valuesTitle.value = vt.contentTitle || valuesTitle.value;
    const ct = findLabel("about_contact");
    if (ct) {
      contactTitle.value = ct.contentTitle || contactTitle.value;
      if (ct.contentText) contactSub.value = ct.contentText;
    }

    // Value cards — fallback to platformPromises if backend returns empty
    const backendValues = (data.story || [])
      .filter((s) => s.sectionKey.startsWith("value_"))
      .sort((a, b) => a.sortOrder - b.sortOrder)
      .map((v) => ({
        icon: parseExtra(v.extraData).icon || "",
        title: v.contentTitle || "",
        text: v.contentText || "",
      }));
    if (backendValues.length > 0) {
      platformPromises.value = backendValues;
    }

    // Contact
    const contactRow = (data.contact || [])[0];
    if (contactRow) {
      const extra = parseExtra(contactRow.extraData);
      contact.value = {
        phone: extra.phone || "",
        email: extra.email || "",
        address: extra.address || "",
        phoneNote: extra.phoneNote || "",
        emailNote: extra.emailNote || "",
      };
    }
  } catch {
    /* ignore */
  }
});
</script>

<style scoped>
.about-page {
  background: var(--color-bg);
}

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
  color: rgba(255, 255, 255, 0.6);
}

.about-sub {
  font-size: var(--text-lg);
  color: rgba(255, 255, 255, 0.6);
}

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
  overflow: hidden;
}

.about-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

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

.value-card {
  text-align: center;
  padding: var(--space-6);
}

.value-icon {
  font-size: 40px;
  display: block;
  margin-bottom: var(--space-4);
}

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

.contact-icon {
  font-size: 36px;
  display: block;
  margin-bottom: var(--space-4);
}

.contact-card h3 {
  font-size: var(--text-md);
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: var(--space-3);
}

.contact-card p {
  font-size: var(--text-sm);
  color: var(--color-text-secondary);
}

.contact-note {
  font-size: var(--text-xs) !important;
  color: var(--color-text-tertiary) !important;
  margin-top: var(--space-1);
}

@media (max-width: 768px) {
  .about-row {
    grid-template-columns: 1fr;
  }

  .values-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .contact-grid {
    grid-template-columns: 1fr;
  }

  .about-title {
    font-size: 2rem;
  }
}
</style>
