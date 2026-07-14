<template>
  <section class="hero">
    <div
      class="hero-track"
      :style="{ transform: `translateX(-${current * 100}%)` }"
    >
      <div class="hero-slide" v-for="(slide, i) in slides" :key="i">
        <div class="hero-bg" :style="{ backgroundColor: slide.bg }"></div>
        <div class="hero-inner">
          <div class="hero-text">
            <p class="hero-tag">{{ slide.tag }}</p>
            <h2 class="hero-title">{{ slide.title }}</h2>
            <p class="hero-desc">{{ slide.desc }}</p>
            <router-link :to="slide.link" class="hero-cta">
              {{ slide.cta }}
              <svg
                width="16"
                height="16"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2.5"
                stroke-linecap="round"
                stroke-linejoin="round"
              >
                <path d="M5 12h14M12 5l7 7-7 7" />
              </svg>
            </router-link>
          </div>
          <div class="hero-visual">
            <div class="hero-visual-inner">
              <img
                v-if="slide.image"
                :src="imgUrl(slide.image)"
                class="hero-image"
                alt=""
              />
              <span v-else class="hero-emoji">{{ slide.emoji || "🛋️" }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Controls -->
    <button class="hero-arrow hero-prev" @click="prev" aria-label="上一张">
      <svg
        width="20"
        height="20"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        stroke-width="2"
      >
        <path d="M15 18l-6-6 6-6" />
      </svg>
    </button>
    <button class="hero-arrow hero-next" @click="next" aria-label="下一张">
      <svg
        width="20"
        height="20"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        stroke-width="2"
      >
        <path d="M9 18l6-6-6-6" />
      </svg>
    </button>

    <!-- Dots -->
    <div class="hero-dots">
      <button
        v-for="(s, i) in slides"
        :key="i"
        :class="{ active: i === current }"
        @click="current = i"
      ></button>
    </div>
  </section>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from "vue";
import { getSiteContent } from "@/api/siteContent.js";
import { imgUrl } from "@/utils/img.js";

const current = ref(0);
const slides = ref([]);

const loadSlides = async () => {
  try {
    const res = await getSiteContent();
    if ((res.success || res.code === 200) && res.data?.carousel) {
      slides.value = res.data.carousel.map((s) => {
        const extra = parseExtra(s.extraData);
        return {
          bg: extra.bg || "#e8e0d5",
          tag: extra.tag || "",
          title: s.contentTitle || "",
          desc: s.contentText || "",
          cta: extra.cta || "了解更多",
          link: "/type/0",
          emoji: extra.emoji || "",
          image: s.imageUrl || "",
        };
      });
    }
  } catch {
    /* keep defaults */
  }
};

const parseExtra = (str) => {
  try {
    return JSON.parse(str) || {};
  } catch {
    return {};
  }
};

let timer = null;
const startTimer = () => {
  if (slides.value.length > 0)
    timer = setInterval(() => {
      current.value = (current.value + 1) % slides.value.length;
    }, 5000);
};
const stopTimer = () => {
  clearInterval(timer);
};
const prev = () => {
  stopTimer();
  current.value =
    (current.value - 1 + slides.value.length) % slides.value.length;
  startTimer();
};
const next = () => {
  stopTimer();
  current.value = (current.value + 1) % slides.value.length;
  startTimer();
};

onMounted(async () => {
  await loadSlides();
  startTimer();
});
onBeforeUnmount(stopTimer);
</script>

<style scoped>
.hero {
  position: relative;
  overflow: hidden;
  background: var(--color-bg);
}
.hero-track {
  display: flex;
  transition: transform 0.6s cubic-bezier(0.25, 0.1, 0.25, 1);
}
.hero-slide {
  min-width: 100%;
  position: relative;
}
.hero-bg {
  position: absolute;
  inset: 0;
}
.hero-inner {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: var(--space-12) var(--space-6) var(--space-10);
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-12);
  align-items: center;
  position: relative;
  z-index: 1;
  min-height: 420px;
}

/* Text */
.hero-tag {
  font-size: var(--text-xs);
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.15em;
  color: var(--color-accent);
  margin-bottom: var(--space-4);
}
.hero-title {
  font-size: 3rem;
  font-weight: 700;
  color: var(--color-text-primary);
  line-height: 1.15;
  margin-bottom: var(--space-4);
  font-family: var(--font-serif);
  letter-spacing: -0.02em;
}
.hero-desc {
  font-size: var(--text-lg);
  color: var(--color-text-secondary);
  line-height: var(--leading-relaxed);
  margin-bottom: var(--space-8);
  max-width: 400px;
}
.hero-cta {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-6);
  background: var(--color-dark);
  color: #fff;
  font-size: var(--text-sm);
  font-weight: 600;
  border-radius: var(--radius-md);
  text-decoration: none;
  transition: background var(--transition-fast);
}
.hero-cta:hover {
  background: var(--color-dark-hover);
}

/* Visual */
.hero-visual {
  display: flex;
  align-items: center;
  justify-content: center;
}
.hero-visual-inner {
  width: 280px;
  height: 280px;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.hero-emoji {
  font-size: 100px;
}

/* Arrows */
.hero-arrow {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 44px;
  height: 44px;
  background: var(--color-surface);
  border: 1px solid var(--color-border-light);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-text-secondary);
  cursor: pointer;
  z-index: 2;
  transition: all var(--transition-fast);
}
.hero-arrow:hover {
  color: var(--color-text-primary);
  border-color: var(--color-border);
  box-shadow: var(--shadow-sm);
}
.hero-prev {
  left: var(--space-6);
}
.hero-next {
  right: var(--space-6);
}

/* Dots */
.hero-dots {
  position: absolute;
  bottom: var(--space-6);
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: var(--space-2);
}
.hero-dots button {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.2);
  border: none;
  cursor: pointer;
  transition: all var(--transition-fast);
  padding: 0;
}
.hero-dots button.active {
  background: var(--color-dark);
  width: 24px;
  border-radius: var(--radius-full);
}

@media (max-width: 768px) {
  .hero-inner {
    grid-template-columns: 1fr;
    text-align: center;
    min-height: 360px;
    padding: var(--space-10) var(--space-4) var(--space-8);
  }
  .hero-title {
    font-size: 2rem;
  }
  .hero-desc {
    margin-left: auto;
    margin-right: auto;
  }
  .hero-visual {
    display: none;
  }
  .hero-arrow {
    width: 36px;
    height: 36px;
  }
  .hero-arrow svg {
    width: 16px;
    height: 16px;
  }
  .hero-prev {
    left: var(--space-3);
  }
  .hero-next {
    right: var(--space-3);
  }
}
</style>
