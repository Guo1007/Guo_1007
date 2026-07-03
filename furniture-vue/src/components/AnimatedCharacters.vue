<template>
  <!--
    AnimatedCharacters - 动态卡通小人组件
    variant="login"  - 登录页配色
    variant="register" - 注册页配色（颜色/顺序/眼睛大小都不同）
  -->
  <div ref="containerRef" class="relative select-none" style="width:550px; height:400px; perspective:800px;">

    <!-- ====== 角色 A（最高） ====== -->
    <div
        ref="purpleRef"
        class="absolute transition-all duration-300 ease-out"
        :style="{
        bottom: '0',
        left: cfg.a.left + 'px',
        width: cfg.a.width + 'px',
        height: cfg.a.height + 'px',
        background: cfg.a.color,
        borderRadius: cfg.a.radius,
        zIndex: 1,
        transformOrigin: 'bottom center',
        willChange: 'transform',
        ...purpleStyle
      }"
    >
      <div
          class="absolute flex transition-all duration-200 ease-out"
          :style="{ gap: cfg.a.eyeGap + 'px', ...facePosition('purple') }"
      >
        <div class="eyeball" :style="eyeballSize(cfg.a.eyeSize)">
          <div class="eyeball-pupil" :style="pupilStyle"></div>
        </div>
        <div class="eyeball" :style="eyeballSize(cfg.a.eyeSize)">
          <div class="eyeball-pupil" :style="pupilStyle"></div>
        </div>
      </div>
    </div>

    <!-- ====== 角色 B（中高） ====== -->
    <div
        ref="blackRef"
        class="absolute transition-all duration-300 ease-out"
        :style="{
        bottom: '0',
        left: cfg.b.left + 'px',
        width: cfg.b.width + 'px',
        height: cfg.b.height + 'px',
        background: cfg.b.color,
        borderRadius: cfg.b.radius,
        zIndex: 2,
        transformOrigin: 'bottom center',
        willChange: 'transform',
        ...blackStyle
      }"
    >
      <div
          class="absolute flex transition-all duration-200 ease-out"
          :style="{ gap: cfg.b.eyeGap + 'px', ...facePosition('black') }"
      >
        <div class="eyeball" :style="eyeballSize(cfg.b.eyeSize)">
          <div class="eyeball-pupil" :style="pupilStyle"></div>
        </div>
        <div class="eyeball" :style="eyeballSize(cfg.b.eyeSize)">
          <div class="eyeball-pupil" :style="pupilStyle"></div>
        </div>
      </div>
    </div>

    <!-- ====== 角色 C（最矮最宽） ====== -->
    <div
        ref="orangeRef"
        class="absolute transition-all duration-300 ease-out"
        :style="{
        bottom: '0',
        left: cfg.c.left + 'px',
        width: cfg.c.width + 'px',
        height: cfg.c.height + 'px',
        background: cfg.c.color,
        borderRadius: cfg.c.radius,
        zIndex: 3,
        transformOrigin: 'bottom center',
        willChange: 'transform',
        ...orangeStyle
      }"
    >
      <div
          class="absolute flex transition-all duration-200 ease-out"
          :style="{ gap: cfg.c.eyeGap + 'px', ...facePosition('orange') }"
      >
        <div class="pupil-dot" :style="pupilDotStyle(cfg.c.dotSize)"></div>
        <div class="pupil-dot" :style="pupilDotStyle(cfg.c.dotSize)"></div>
      </div>
    </div>

    <!-- ====== 角色 D（中等） ====== -->
    <div
        ref="yellowRef"
        class="absolute transition-all duration-300 ease-out"
        :style="{
        bottom: '0',
        left: cfg.d.left + 'px',
        width: cfg.d.width + 'px',
        height: cfg.d.height + 'px',
        background: cfg.d.color,
        borderRadius: cfg.d.radius,
        zIndex: 4,
        transformOrigin: 'bottom center',
        willChange: 'transform',
        ...yellowStyle
      }"
    >
      <div
          class="absolute flex transition-all duration-200 ease-out"
          :style="{ gap: cfg.d.eyeGap + 'px', ...facePosition('yellow') }"
      >
        <div class="pupil-dot" :style="pupilDotStyle(cfg.d.dotSize)"></div>
        <div class="pupil-dot" :style="pupilDotStyle(cfg.d.dotSize)"></div>
      </div>
      <!-- 嘴巴 -->
      <div
          class="absolute transition-all duration-200 ease-out"
          :style="mouthStyle"
      ></div>
    </div>

  </div>
</template>

<script setup>
/**
 * AnimatedCharacters - 动态卡通小人
 * 支持 login / register 两种配色方案
 */
import {computed, onBeforeUnmount, onMounted, ref} from 'vue'

const props = defineProps({
  mouseX: {type: Number, default: 0},
  mouseY: {type: Number, default: 0},
  isTyping: {type: Boolean, default: false},
  showPassword: {type: Boolean, default: false},
  /** 'login' | 'register' */
  variant: {type: String, default: 'login'}
})

/**
 * 随机生成 4 个小人的颜色
 * 使用 HSL 色相均匀分布，每次访问都不同
 */
function randomColors() {
  const baseHue = Math.random() * 360
  return [0, 1, 2, 3].map(i => {
    const h = Math.round((baseHue + i * 90) % 360)
    return `hsl(${h}, 65%, 55%)`
  })
}

/**
 * 两种配色方案
 * 颜色在组件初始化时随机生成，每个小人色相均匀分布
 */
const loginColors = randomColors()
const registerColors = randomColors()

const configs = {
  login: {
    // A: 最高角色
    a: {
      left: 70,
      width: 180,
      height: 400,
      color: loginColors[0],
      radius: '10px 10px 0 0',
      eyeSize: 18,
      eyeGap: 32,
      dotSize: 0
    },
    // B: 中高角色
    b: {
      left: 240,
      width: 120,
      height: 310,
      color: loginColors[1],
      radius: '8px 8px 0 0',
      eyeSize: 16,
      eyeGap: 24,
      dotSize: 0
    },
    // C: 最矮最宽（圆形）
    c: {
      left: 0,
      width: 240,
      height: 200,
      color: loginColors[2],
      radius: '120px 120px 0 0',
      eyeSize: 0,
      eyeGap: 32,
      dotSize: 12
    },
    // D: 中等（拱形+嘴巴）
    d: {
      left: 310,
      width: 140,
      height: 230,
      color: loginColors[3],
      radius: '70px 70px 0 0',
      eyeSize: 0,
      eyeGap: 24,
      dotSize: 12
    },
    // 面部基准位置
    faces: {
      purple: {left: 45, top: 40},
      black: {left: 26, top: 32},
      orange: {left: 82, top: 90},
      yellow: {left: 52, top: 40}
    }
  },
  register: {
    // A: 最高角色 - 青色，位置偏右
    a: {
      left: 120,
      width: 160,
      height: 380,
      color: registerColors[0],
      radius: '8px 8px 0 0',
      eyeSize: 14,
      eyeGap: 28,
      dotSize: 0
    },
    // B: 中高角色 - 粉色，位置偏左
    b: {
      left: 30,
      width: 130,
      height: 320,
      color: registerColors[1],
      radius: '10px 10px 0 0',
      eyeSize: 20,
      eyeGap: 30,
      dotSize: 0
    },
    // C: 最矮最宽（圆形） - 薄荷绿
    c: {
      left: 200,
      width: 220,
      height: 190,
      color: registerColors[2],
      radius: '110px 110px 0 0',
      eyeSize: 0,
      eyeGap: 28,
      dotSize: 10
    },
    // D: 中等（拱形+嘴巴） - 蓝色
    d: {
      left: 60,
      width: 130,
      height: 210,
      color: registerColors[3],
      radius: '65px 65px 0 0',
      eyeSize: 0,
      eyeGap: 22,
      dotSize: 14
    },
    // 面部基准位置（因为尺寸变了，位置也要调整）
    faces: {
      purple: {left: 38, top: 35},
      black: {left: 30, top: 28},
      orange: {left: 75, top: 80},
      yellow: {left: 38, top: 30}
    }
  }
}

const cfg = computed(() => configs[props.variant] || configs.login)

const containerRef = ref(null)
const purpleRef = ref(null)
const blackRef = ref(null)
const orangeRef = ref(null)
const yellowRef = ref(null)

// 内部鼠标追踪（用于眼球跟随）
const internalMouse = ref({x: 0, y: 0})
let rafId = 0

function tick() {
  internalMouse.value = {x: props.mouseX, y: props.mouseY}
  rafId = requestAnimationFrame(tick)
}

onMounted(() => {
  rafId = requestAnimationFrame(tick)
})

onBeforeUnmount(() => {
  cancelAnimationFrame(rafId)
})

/**
 * 计算眼球/瞳孔偏移
 * 以元素中心为原点，限制在 maxDist 范围内
 */
function calcEyeOffset(el, maxDist = 5) {
  if (!el || typeof window === 'undefined') return {x: 0, y: 0}
  const rect = el.getBoundingClientRect()
  const cx = rect.left + rect.width / 2
  const cy = rect.top + rect.height / 2
  const dx = internalMouse.value.x - cx
  const dy = internalMouse.value.y - cy
  const dist = Math.min(Math.sqrt(dx * dx + dy * dy), maxDist)
  const angle = Math.atan2(dy, dx)
  return {
    x: Math.cos(angle) * dist,
    y: Math.sin(angle) * dist
  }
}

/** 眼球样式（紫色、黑色用） */
const pupilStyle = computed(() => {
  const offset = calcEyeOffset(containerRef.value, 5)
  return {
    transform: `translate(${offset.x.toFixed(2)}px, ${offset.y.toFixed(2)}px)`
  }
})


/** 眼球容器尺寸 */
function eyeballSize(size) {
  return {
    width: `${size}px`,
    height: `${size}px`,
    borderRadius: '50%',
    backgroundColor: 'white',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    overflow: 'hidden'
  }
}

/** 瞳孔点尺寸 */
function pupilDotStyle(size) {
  const offset = calcEyeOffset(containerRef.value, 5)
  return {
    width: `${size}px`,
    height: `${size}px`,
    background: '#2D2D2D',
    borderRadius: '50%',
    willChange: 'transform',
    transition: 'transform 0.08s ease-out',
    transform: `translate(${offset.x.toFixed(2)}px, ${offset.y.toFixed(2)}px)`
  }
}

/**
 * 计算角色 skewX（身体倾斜跟随鼠标）
 */
function calcBodySkew(el) {
  if (!el || typeof window === 'undefined') return 0
  const rect = el.getBoundingClientRect()
  const cx = rect.left + rect.width / 2
  const dx = internalMouse.value.x - cx
  return Math.max(-6, Math.min(6, -dx / 120))
}

/**
 * 计算面部偏移（跟随鼠标方向）
 */
function calcFaceOffset(el) {
  if (!el || typeof window === 'undefined') return {x: 0, y: 0}
  const rect = el.getBoundingClientRect()
  const cx = rect.left + rect.width / 2
  const cy = rect.top + rect.height / 3
  const dx = internalMouse.value.x - cx
  const dy = internalMouse.value.y - cy
  return {
    x: Math.max(-15, Math.min(15, dx / 20)),
    y: Math.max(-10, Math.min(10, dy / 30))
  }
}

/** 紫色角色样式 */
const purpleStyle = computed(() => {
  if (props.showPassword) {
    return {
      transform: 'skewX(0deg) translateX(0px)',
      height: '400px'
    }
  }
  const skew = calcBodySkew(purpleRef.value)
  if (props.isTyping) {
    return {
      transform: `skewX(${skew - 12}deg) translateX(40px)`,
      height: '440px'
    }
  }
  return {
    transform: `skewX(${skew}deg) translateX(0px)`,
    height: '400px'
  }
})

/** 黑色角色样式 */
const blackStyle = computed(() => {
  if (props.showPassword) {
    return {transform: 'skewX(0deg) translateX(0px)'}
  }
  const skew = calcBodySkew(blackRef.value)
  if (props.isTyping) {
    return {transform: `skewX(${skew * 1.5}deg) translateX(0px)`}
  }
  return {transform: `skewX(${skew}deg) translateX(0px)`}
})

/** 橙色角色样式 */
const orangeStyle = computed(() => {
  if (props.showPassword) {
    return {transform: 'skewX(0deg)'}
  }
  const skew = calcBodySkew(orangeRef.value)
  return {transform: `skewX(${skew}deg)`}
})

/** 黄色角色样式 */
const yellowStyle = computed(() => {
  if (props.showPassword) {
    return {transform: 'skewX(0deg)'}
  }
  const skew = calcBodySkew(yellowRef.value)
  return {transform: `skewX(${skew}deg)`}
})

/**
 * 面部位置计算
 * 使用 cfg.faces 中的基准值 + 鼠标偏移
 */
function facePosition(char) {
  const faces = cfg.value.faces
  if (props.showPassword) {
    // 转身时面部移向左侧（背对右侧表单）
    const f = faces[char] || {left: 0, top: 0}
    return {left: `${f.left - 25}px`, top: `${f.top - 5}px`}
  }

  const el = char === 'purple' ? purpleRef.value
      : char === 'black' ? blackRef.value
          : char === 'orange' ? orangeRef.value
              : yellowRef.value

  const offset = calcFaceOffset(el)
  const f = faces[char] || {left: 0, top: 0}

  if (char === 'purple' && props.isTyping) {
    return {
      left: `${f.left + Math.min(25, offset.x * 1.5 + 10)}px`,
      top: `${f.top + offset.y}px`
    }
  }

  return {
    left: `${f.left + offset.x}px`,
    top: `${f.top + offset.y}px`
  }
}

/** 嘴巴样式 - 根据 cfg 动态计算 */
const mouthStyle = computed(() => {
  const el = yellowRef.value
  const offset = calcFaceOffset(el)
  const d = cfg.value.d
  const mouthWidth = Math.round(d.width * 0.57)
  const mouthTop = Math.round(d.height * 0.38)
  const mouthLeft = Math.round((d.width - mouthWidth) / 2)
  return {
    width: `${mouthWidth}px`,
    height: '4px',
    backgroundColor: '#2D2D2D',
    borderRadius: '9999px',
    left: `${mouthLeft + offset.x}px`,
    top: `${mouthTop + offset.y}px`,
    opacity: props.showPassword ? 0 : 1
  }
})
</script>

<style scoped>
/* 瞳孔小黑点（橙色、黄色角色用） */
.pupil-dot {
  width: 12px;
  height: 12px;
  background: #2D2D2D;
  border-radius: 50%;
  will-change: transform;
  transition: transform 0.08s ease-out;
}

/* 眼球内瞳孔（紫色、黑色角色用） */
.eyeball-pupil {
  width: 7px;
  height: 7px;
  background: #2D2D2D;
  border-radius: 50%;
  will-change: transform;
  transition: transform 0.08s ease-out;
}
</style>
