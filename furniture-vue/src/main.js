import {createApp} from 'vue'
import {createPinia} from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './index.css'
import App from './App.vue'
import router from './router'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import '@/styles/views/home.scss'
import '@/styles/views/furniture.scss'
import '@/styles/views/auth.scss'
import '@/styles/views/userOrder.scss'
import '@/styles/views/payView.scss'
import '@/styles/views/profile.scss'
import '@/styles/views/cart.scss'
import '@/styles/responsive.scss'

const app = createApp(App)

app.use(ElementPlus, {
    locale: zhCn,
})

app.use(createPinia())
app.use(router)

app.mount('#app')