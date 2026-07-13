<template>
  <div class="manage-page">
    <h2 class="page-title">首页内容管理</h2>

    <el-tabs v-model="activeGroup" @tab-change="loadData">
      <el-tab-pane label="轮播管理" name="carousel" />
      <el-tab-pane label="品牌故事" name="story" />
      <el-tab-pane label="页面文案" name="label" />
      <el-tab-pane label="联系我们" name="contact" />
      <el-tab-pane label="服务保障" name="service" />
      <el-tab-pane label="系统品牌" name="brand" />
    </el-tabs>

    <!-- carousel / story / brand cards -->
    <div class="sc-grid" v-if="['carousel','story','brand'].includes(activeGroup)">
      <div class="sc-card" v-for="item in filteredList" :key="item.id">
        <div class="sc-card-hd">
          <span class="sc-key">{{ keyLabel(item.sectionKey) }}</span>
          <el-tag v-if="activeGroup !== 'brand'" :type="item.isActive === 1 ? 'success' : 'info'" size="small">
            {{ item.isActive === 1 ? '启用' : '停用' }}
          </el-tag>
        </div>

        <el-form label-width="80px" size="default" class="sc-form">
          <!-- Image: only for carousel slides and brand_image -->
          <el-form-item v-if="showImageFor(item.sectionKey)" label="图片">
            <div class="image-upload-row">
              <img v-if="item.imageUrl" :src="imgUrl(item.imageUrl)" class="sc-preview" />
              <span v-else class="sc-no-img">无图片</span>
              <input type="file" accept="image/*" :ref="el => fileInputs[item.id] = el" style="display:none"
                @change="e => onUpload(e, item)" />
              <el-button size="small" @click="triggerUpload(item)" :loading="uploadLoading[item.id]">上传</el-button>
            </div>
          </el-form-item>

          <!-- Value card icon -->
          <el-form-item v-if="item.sectionKey.startsWith('value_')" label="图标">
            <el-input v-model="extraCache[item.id].icon" placeholder="如：🌳" />
          </el-form-item>

          <!-- Title: not for brand_image (image-only) -->
          <el-form-item v-if="item.sectionKey !== 'brand_image'" label="标题">
            <el-input v-model="item.contentTitle" :placeholder="item.sectionKey.startsWith('value_') ? '如：天然选材' : '标题'" />
          </el-form-item>

          <!-- Description: for brand_intro and value cards -->
          <el-form-item v-if="showDescFor(item.sectionKey)" label="描述">
            <el-input v-model="item.contentText" type="textarea" :rows="item.sectionKey === 'brand_intro' ? 6 : 2" placeholder="描述" />
          </el-form-item>

          <!-- Carousel extras -->
          <template v-if="item.sectionKey.startsWith('hero_')">
            <el-form-item label="小标签">
              <el-input v-model="extraCache[item.id].tag" placeholder="如：2026新品上市" />
            </el-form-item>
            <el-form-item label="按钮文字">
              <el-input v-model="extraCache[item.id].cta" placeholder="如：立即探索" />
            </el-form-item>
            <el-form-item label="背景色">
              <el-color-picker v-model="extraCache[item.id].bg" size="small" />
            </el-form-item>
          </template>

          <el-form-item label="排序">
            <el-input-number v-model="item.sortOrder" :min="0" size="small" />
          </el-form-item>
        </el-form>

        <div class="sc-card-actions">
          <el-button type="primary" size="small" @click="saveItem(item)">保存</el-button>
          <el-button v-if="activeGroup !== 'brand'" size="small" @click="toggleItem(item)">
            {{ item.isActive === 1 ? '停用' : '启用' }}
          </el-button>
        </div>
      </div>
    </div>

    <!-- contact — single card -->
    <div class="sc-card" v-if="activeGroup === 'contact' && filteredList.length > 0">
      <div class="sc-card-hd">
        <span class="sc-key">联系方式</span>
      </div>
      <el-form label-width="80px" size="default" class="sc-form">
        <el-form-item label="客服热线">
          <el-input v-model="contactExtra.phone" placeholder="400-888-8888" />
        </el-form-item>
        <el-form-item label="热线备注">
          <el-input v-model="contactExtra.phoneNote" placeholder="周一至周日 9:00-21:00" />
        </el-form-item>
        <el-form-item label="电子邮箱">
          <el-input v-model="contactExtra.email" placeholder="service@woodspace.com" />
        </el-form-item>
        <el-form-item label="邮箱备注">
          <el-input v-model="contactExtra.emailNote" placeholder="24小时内回复" />
        </el-form-item>
        <el-form-item label="总部地址">
          <el-input v-model="contactExtra.address" type="textarea" :rows="2" placeholder="广东省深圳市..." />
        </el-form-item>
      </el-form>
      <div class="sc-card-actions">
        <el-button type="primary" size="small" @click="saveContact">保存</el-button>
      </div>
    </div>

    <!-- label cards -->
    <div class="sc-grid" v-if="activeGroup === 'label'">
      <div class="sc-card" v-for="item in filteredList" :key="item.id">
        <div class="sc-card-hd">
          <span class="sc-key">{{ keyLabel(item.sectionKey) }}</span>
        </div>
        <el-form label-width="80px" size="default" class="sc-form">
          <el-form-item v-if="item.contentTitle !== null && item.contentTitle !== undefined" label="标题">
            <el-input v-model="item.contentTitle" placeholder="标题" />
          </el-form-item>
          <el-form-item v-if="item.contentText !== null && item.contentText !== undefined" label="描述">
            <el-input v-model="item.contentText" type="textarea" :rows="2" placeholder="副标题或描述" />
          </el-form-item>
          <el-form-item label="排序">
            <el-input-number v-model="item.sortOrder" :min="0" size="small" />
          </el-form-item>
        </el-form>
        <div class="sc-card-actions">
          <el-button type="primary" size="small" @click="saveItem(item)">保存</el-button>
        </div>
      </div>
    </div>

    <!-- brand cards -->
    <div class="sc-grid" v-if="activeGroup === 'brand'">
      <div class="sc-card" v-for="item in filteredList" :key="item.id">
        <div class="sc-card-hd">
          <span class="sc-key">{{ keyLabel(item.sectionKey) }}</span>
        </div>
        <el-form label-width="80px" size="default" class="sc-form">
          <el-form-item v-if="item.sectionKey === 'system_logo'" label="Logo图片">
            <div class="image-upload-row">
              <img v-if="item.imageUrl" :src="imgUrl(item.imageUrl)" class="sc-preview" />
              <span v-else class="sc-no-img">无图片</span>
              <input type="file" accept="image/*" :ref="el => fileInputs[item.id] = el" style="display:none"
                @change="e => onUpload(e, item)" />
              <el-button size="small" @click="triggerUpload(item)" :loading="uploadLoading[item.id]">上传</el-button>
            </div>
          </el-form-item>
          <el-form-item v-if="item.sectionKey !== 'system_logo'" label="内容">
            <el-input v-model="item.contentTitle" placeholder="请输入" />
          </el-form-item>
        </el-form>
        <div class="sc-card-actions">
          <el-button type="primary" size="small" @click="saveItem(item)">保存</el-button>
        </div>
      </div>
    </div>

    <!-- service cards (icon + title + desc only) -->
    <div class="sc-grid" v-if="activeGroup === 'service'">
      <div class="sc-card" v-for="item in filteredList" :key="item.id">
        <div class="sc-card-hd">
          <span class="sc-key">{{ keyLabel(item.sectionKey) }}</span>
        </div>
        <el-form label-width="60px" size="default" class="sc-form">
          <el-form-item label="图标">
            <el-input v-model="extraCache[item.id].icon" placeholder="如：🚚" style="width:120px" />
          </el-form-item>
          <el-form-item label="标题">
            <el-input v-model="item.contentTitle" placeholder="如：免费配送" />
          </el-form-item>
          <el-form-item label="描述">
            <el-input v-model="item.contentText" placeholder="如：全国包邮，送货上门" />
          </el-form-item>
          <el-form-item label="排序">
            <el-input-number v-model="item.sortOrder" :min="0" size="small" />
          </el-form-item>
        </el-form>
        <div class="sc-card-actions">
          <el-button type="primary" size="small" @click="saveItem(item)">保存</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getAdminSiteContentList, saveSiteContent, toggleSiteContent, uploadSiteContentImage } from '@/api/siteContent.js'
import { imgUrl } from '@/utils/img.js'

const activeGroup = ref('carousel')
const list = ref([])
const extraCache = reactive({})
const contactExtra = reactive({ phone: '', phoneNote: '', email: '', emailNote: '', address: '' })
const fileInputs = reactive({})
const uploadLoading = reactive({})

const parseExtra = (str) => {
  try { return JSON.parse(str) || {} } catch { return {} }
}

// Which entries support image upload
const showImageFor = (key) => ['hero_1','hero_2','hero_3','brand_image','system_logo'].includes(key)
// Which entries have description text
const showDescFor = (key) => key === 'brand_intro' || key.startsWith('value_')

const keyLabel = (key) => {
  const map = {
    hero_1: '轮播图 ①', hero_2: '轮播图 ②', hero_3: '轮播图 ③',
    brand_intro: '品牌介绍', brand_image: '品牌图片',
    value_1: '核心理念 ① — 天然选材', value_2: '核心理念 ② — 匠心工艺',
    value_3: '核心理念 ③ — 简约设计', value_4: '核心理念 ④ — 绿色环保',
    contact_info: '联系方式',
    service_1: '服务项 ① — 免费配送', service_2: '服务项 ② — 7天无理由',
    service_3: '服务项 ③ — 质保3年', service_4: '服务项 ④ — 定制服务',
    home_categories: '首页—分类区块标题', home_products: '首页—精选好物标题',
    home_brand_label: '首页—关于我们标签',
    about_hero_tag: '关于页—顶部标签', about_hero_sub: '关于页—顶部副标题',
    about_story_title: '关于页—故事区标题', about_values_title: '关于页—理念区标题',
    about_contact: '关于页—联系区标题',
    system_name: '系统名称', system_tagline: '系统标语', system_logo: '系统Logo'
  }
  return map[key] || key
}

const filteredList = computed(() => {
  return list.value
    .filter(i => i.sectionGroup === activeGroup.value)
    .sort((a, b) => a.sortOrder - b.sortOrder)
})

const loadData = async () => {
  try {
    const res = await getAdminSiteContentList()
    if ((res.success || res.code === 200) && Array.isArray(res.data)) {
      list.value = res.data
      list.value.forEach(item => {
        extraCache[item.id] = parseExtra(item.extraData)
      })
      // contact
      const ct = res.data.find(i => i.sectionKey === 'contact_info')
      if (ct) Object.assign(contactExtra, parseExtra(ct.extraData))
    }
  } catch { /* ignore */ }
}

const saveItem = async (item) => {
  item.extraData = JSON.stringify(extraCache[item.id] || {})
  try {
    const res = await saveSiteContent({ ...item })
    if (res.success || res.code === 200) ElMessage.success('保存成功')
    else ElMessage.error(res.msg || '保存失败')
  } catch { ElMessage.error('保存失败') }
}

const saveContact = async () => {
  const ct = list.value.find(i => i.sectionKey === 'contact_info')
  if (!ct) return
  ct.extraData = JSON.stringify({ ...contactExtra })
  try {
    const res = await saveSiteContent({ ...ct })
    if (res.success || res.code === 200) ElMessage.success('保存成功')
    else ElMessage.error(res.msg || '保存失败')
  } catch { ElMessage.error('保存失败') }
}

const toggleItem = async (item) => {
  try {
    const res = await toggleSiteContent(item.id)
    if (res.success || res.code === 200) {
      item.isActive = res.data
      ElMessage.success(item.isActive === 1 ? '已启用' : '已停用')
    }
  } catch { ElMessage.error('操作失败') }
}

const triggerUpload = (item) => {
  fileInputs[item.id]?.click()
}

const onUpload = async (e, item) => {
  const file = e.target.files[0]
  if (!file) return
  uploadLoading[item.id] = true
  try {
    const res = await uploadSiteContentImage(file)
    if ((res.success || res.code === 200) && res.data) {
      item.imageUrl = res.data
      ElMessage.success('上传成功')
    } else {
      ElMessage.error(res.msg || '上传失败')
    }
  } catch { ElMessage.error('上传失败') }
  finally { uploadLoading[item.id] = false }
  e.target.value = ''
}

onMounted(loadData)
</script>

<style scoped>
.page-title { margin: 0 0 24px 0; font-size: 20px; color: var(--color-text-primary); }

.sc-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(380px, 1fr)); gap: 20px; }
.sc-card {
  background: #fff; border: 1px solid var(--color-border-light);
  border-radius: var(--radius-lg); padding: 20px;
}
.sc-card-hd { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.sc-key { font-size: 13px; font-weight: 600; color: var(--color-text-secondary); font-family: monospace; }
.sc-form :deep(.el-form-item) { margin-bottom: 12px; }
.sc-form :deep(.el-form-item__label) { font-size: 12px; }
.sc-card-actions { display: flex; gap: 8px; justify-content: flex-end; margin-top: 16px; padding-top: 16px; border-top: 1px solid var(--color-border-light); }

.image-upload-row { display: flex; align-items: center; gap: 8px; }
.sc-preview { width: 48px; height: 48px; object-fit: cover; border-radius: 6px; background: #f5f5f5; }
.sc-no-img { width: 48px; height: 48px; display: flex; align-items: center; justify-content: center; background: #f5f5f5; border-radius: 6px; color: #ccc; font-size: 11px; }
</style>
