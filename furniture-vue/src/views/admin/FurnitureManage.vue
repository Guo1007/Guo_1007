<template>
    <div class="manage-page">
        <h2 class="page-title">🛋️ 家具管理</h2>

        <!-- 搜索栏 -->
        <div class="search-bar">
            <el-select v-model="searchForm.typeId" placeholder="分类" clearable style="width: 150px">
                <el-option v-for="type in typeList" :key="type.id" :label="type.name" :value="type.id" />
            </el-select>
            <el-input v-model="searchForm.fName" placeholder="家具名称" clearable style="width: 200px; margin-left: 10px"
                @keyup.enter="handleSearch" />
            <el-select v-model="searchForm.stockStatus" placeholder="库存状态" clearable
                style="width: 150px; margin-left: 10px">
                <el-option label="有库存" value="in_stock" />
                <el-option label="库存紧张" value="low_stock" />
                <el-option label="无库存" value="out_stock" />
            </el-select>
            <el-input v-model="searchForm.brand" placeholder="品牌" clearable style="width: 150px; margin-left: 10px" />
            <el-button type="primary" style="margin-left: 10px" @click="handleSearch">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
            <el-button type="success" style="margin-left: auto" @click="handleAdd">+ 新增家具</el-button>
        </div>

        <!-- 表格 -->
        <el-table :data="furnitureList" v-loading="loading" border>
            <el-table-column label="图片" width="80">
                <template #default="{ row }">
                    <img v-if="row.fIcon"
                         :src="imgUrl(row.fIcon)"
                        class="table-img" @error="$event.target.style.display = 'none'" />
                    <span v-else>🪑</span>
                </template>
            </el-table-column>
            <el-table-column prop="fName" label="家具名称" min-width="150" />
            <el-table-column prop="brand" label="品牌" width="120" />
            <el-table-column prop="price" label="价格" width="120">
                <template #default="{ row }">
                    ¥{{ row.price }}
                </template>
            </el-table-column>
            <el-table-column prop="stock" label="库存" width="100">
                <template #default="{ row }">
                    <el-tag :type="getStockType(row.stock)">
                        {{ row.stock }}
                    </el-tag>
                </template>
            </el-table-column>
            <el-table-column prop="typeId" label="分类" width="100">
                <template #default="{ row }">
                    {{ getTypeName(row.typeId) }}
                </template>
            </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
                <template #default="{ row }">
                    <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
                  <el-button type="success" size="small" @click="openSpecDialog(row)">规格</el-button>
                    <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
                </template>
            </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination">
            <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize"
                :page-sizes="[10, 20, 50, 100]" :total="total" layout="total, sizes, prev, pager, next, jumper"
                @size-change="handleSizeChange" @current-change="loadData" />
        </div>

        <!-- 新增/编辑 弹窗 -->
        <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑家具' : '新增家具'" width="600px">
            <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
                <!-- 隐藏 ID，仅编辑时使用 -->
                <el-form-item label="ID" v-if="isEdit">
                    <el-input v-model="form.id" disabled />
                </el-form-item>

                <el-form-item label="名称" prop="fName">
                    <el-input v-model="form.fName" placeholder="请输入家具名称" />
                </el-form-item>

                <!-- 图片上传改为组件 -->
                <el-form-item label="图片" prop="fIcon">
                    <div class="upload-container">
                        <el-upload class="avatar-uploader" action="#" :auto-upload="false" :show-file-list="false"
                            :on-change="handleImageChange" accept="image/*">
                            <img v-if="form.fIcon"
                                 :src="imgUrl(form.fIcon)"
                                class="avatar" />
                            <el-icon v-else class="avatar-uploader-icon">
                                <Plus />
                            </el-icon>
                        </el-upload>
                        <div class="upload-tip">
                            <el-button v-if="uploading" type="primary" loading>上传中...</el-button>
                            <span v-else-if="form.fIcon" class="tip-text">点击更换图片</span>
                            <span v-else class="tip-text">点击上传图片</span>
                        </div>
                    </div>
                </el-form-item>

              <!-- 多图片上传 -->
              <el-form-item label="详情图">
                <div class="images-upload-area">
                  <div class="image-thumb" v-for="(img, idx) in imagesList" :key="idx">
                    <img :src="imgUrl(img)" class="thumb-preview" @error="e => e.target.style.display='none'"/>
                    <span class="thumb-delete" @click="removeImage(idx)">✕</span>
                  </div>
                  <div class="image-add-btn" @click="triggerImageUpload" v-if="imagesList.length < 8">
                    <el-icon :size="28">
                      <Plus/>
                    </el-icon>
                    <span v-if="uploadingImages" style="font-size:11px">上传中</span>
                  </div>
                  <input type="file" accept="image/*" ref="multiImageInput" style="display:none"
                         @change="onMultiImageChange"/>
                </div>
                <div class="form-item-tip" style="margin-top:6px">可上传最多8张详情图片，用于商品详情页展示</div>
                </el-form-item>

                <el-form-item label="分类" prop="typeId">
                    <el-select v-model="form.typeId" placeholder="请选择分类" style="width: 100%">
                        <el-option v-for="type in typeList" :key="type.id" :label="type.name" :value="type.id" />
                    </el-select>
                </el-form-item>

                <el-form-item label="价格" prop="price">
                    <el-input-number v-model="form.price" :min="0" :precision="2" style="width: 100%" />
                </el-form-item>

                <el-form-item label="品牌" prop="brand">
                    <el-input v-model="form.brand" placeholder="请输入品牌" />
                </el-form-item>

                <el-form-item label="库存" prop="stock">
                    <el-input-number v-model="form.stock" :min="0" style="width: 100%" />
                </el-form-item>

                <el-form-item label="简介" prop="intro">
                    <el-input v-model="form.intro" type="textarea" :rows="3" placeholder="请输入家具简介" />
                </el-form-item>

              <el-form-item label="商品描述">
                <el-input v-model="form.description" type="textarea" :rows="5"
                          placeholder="请输入商品详细描述（支持多行）"/>
              </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
            </template>
        </el-dialog>

      <!-- 规格管理弹窗 -->
      <el-dialog v-model="specDialogVisible" :title="`规格管理 - ${specFurnitureName}`" width="900px"
                 :close-on-click-modal="false" @close="onSpecDialogClose">
        <el-tabs v-model="specActiveTab">
          <!-- 规格组管理 -->
          <el-tab-pane label="规格设置" name="spec">
            <div v-if="specGroups.length === 0" class="spec-empty-tip">
              <p>暂无规格，点击下方按钮添加规格组（如：颜色、尺寸等）</p>
            </div>
            <div v-for="(group, gIdx) in specGroups" :key="gIdx" class="spec-group-card">
              <div class="spec-group-header">
                <el-input v-model="group.groupName" placeholder="规格组名称（如：颜色）"
                          style="width: 200px" size="small"/>
                <el-input-number v-model="group.sort" :min="0" size="small"
                                 style="width: 100px; margin-left: 10px" controls-position="right"/>
                <el-button type="danger" text size="small" @click="removeSpecGroup(gIdx)"
                           style="margin-left: auto">删除该组
                </el-button>
              </div>
              <div class="spec-values-area">
                <div v-for="(val, vIdx) in group.values" :key="vIdx" class="spec-value-chip">
                  <el-input v-model="val.valueName" placeholder="规格值" size="small"
                            style="width: 120px"/>
                  <el-button text size="small" @click="removeSpecValue(gIdx, vIdx)">✕</el-button>
                </div>
                <el-button size="small" @click="addSpecValue(gIdx)">+ 添加规格值</el-button>
              </div>
            </div>
            <el-button type="primary" plain @click="addSpecGroup" style="margin-top: 10px">
              + 添加规格组
            </el-button>
          </el-tab-pane>

          <!-- SKU管理 -->
          <el-tab-pane label="SKU管理" name="sku">
            <div class="sku-actions">
              <el-button size="small" type="primary" @click="generateSkuTable"
                         :disabled="specGroups.length === 0">
                根据规格生成SKU
              </el-button>
              <el-button size="small" @click="addManualSku">手动添加SKU</el-button>
            </div>
            <el-table :data="skuTableData" border size="small" style="margin-top: 10px" max-height="400">
              <el-table-column label="规格组合" min-width="160">
                <template #default="{ row }">
                  <span v-if="row._specText">{{ row._specText }}</span>
                  <span v-else style="color:#999">无规格</span>
                </template>
              </el-table-column>
              <el-table-column label="SKU编码" width="140">
                <template #default="{ row }">
                  <el-input v-model="row.skuCode" size="small" placeholder="如：SF-MB-3P"/>
                </template>
              </el-table-column>
              <el-table-column label="价格(¥)" width="120">
                <template #default="{ row }">
                  <el-input-number v-model="row.price" :min="0" :precision="2" size="small"
                                   controls-position="right" style="width: 100%"/>
                </template>
              </el-table-column>
              <el-table-column label="库存" width="100">
                <template #default="{ row }">
                  <el-input-number v-model="row.stock" :min="0" size="small"
                                   controls-position="right" style="width: 100%"/>
                </template>
              </el-table-column>
              <el-table-column label="SKU图片" width="80">
                <template #default="{ row }">
                  <el-upload class="sku-img-uploader" action="#" :auto-upload="false"
                             :show-file-list="false"
                             :on-change="(file) => handleSkuImageChange(file, row)" accept="image/*">
                    <img v-if="row.skuImage" :src="imgUrl(row.skuImage)" class="sku-img-thumb"/>
                    <el-icon v-else class="sku-img-add">
                      <Plus/>
                    </el-icon>
                  </el-upload>
                </template>
              </el-table-column>
              <el-table-column label="状态" width="80">
                <template #default="{ row }">
                  <el-switch v-model="row.status" :active-value="1" :inactive-value="0"
                             size="small"/>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="60" fixed="right">
                <template #default="{ $index }">
                  <el-button type="danger" text size="small" @click="removeSku($index)">删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
            <div class="sku-summary" v-if="skuTableData.length > 0">
              <span>共 {{ skuTableData.length }} 个SKU，</span>
              <span>总库存：{{ skuTotalStock }} 件，</span>
              <span>价格区间：¥{{ skuPriceRange }}</span>
            </div>
          </el-tab-pane>
        </el-tabs>
        <template #footer>
          <el-button @click="specDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveSpec" :loading="specSaving">保存规格</el-button>
        </template>
      </el-dialog>
    </div>
</template>

<script setup>
import {computed, onMounted, reactive, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Plus} from '@element-plus/icons-vue'
import {
  addFurniture,
  deleteFurniture,
  editFurniture,
  getFurnitureList,
  uploadFurnitureImage
} from '@/api/admin/furniture.js'
import {getSpecAndSku, saveSpecAndSku} from '@/api/admin/spec.js'
import {getFurnitureTypeList} from '@/api/furniture.js'
import {imgUrl} from '@/utils/img.js'

const loading = ref(false)
const submitLoading = ref(false)
const uploading = ref(false)
const uploadingImages = ref(false)
const furnitureList = ref([])
const typeList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const multiImageInput = ref(null)

// 多图片列表（comma-separated ↔ array）
const imagesList = computed(() => {
  if (!form.images) return []
  return form.images.split(',').map(s => s.trim()).filter(Boolean)
})

// 搜索表单
const searchForm = ref({
    typeId: null,
    fName: '',
    stockStatus: '',
    brand: ''
})

// 编辑/新增表单数据
const form = reactive({
    id: null,
    fName: '',
    fIcon: '',
  images: '',
    typeId: null,
    price: 0,
    brand: '',
    stock: 0,
  intro: '',
  description: ''
})

// ========== 规格管理 ==========
const specDialogVisible = ref(false)
const specFurnitureId = ref(null)
const specFurnitureName = ref('')
const specActiveTab = ref('spec')
const specSaving = ref(false)
const specGroups = ref([])
const skuTableData = ref([])

// 表单校验规则
const rules = {
    fName: [{ required: true, message: '请输入家具名称', trigger: 'blur' }],
    fIcon: [{ required: true, message: '请上传图片', trigger: 'blur' }],
    typeId: [{ required: true, message: '请选择分类', trigger: 'change' }],
    price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
    brand: [{ required: true, message: '请输入品牌', trigger: 'blur' }],
    stock: [{ required: true, message: '请输入库存', trigger: 'blur' }]
}

// --- 方法 ---

// 加载分类
const loadTypes = async () => {
    const res = await getFurnitureTypeList()
    if (res.success || res.code === 200) {
        typeList.value = res.data || []
    }
}

// 加载列表
const loadData = async () => {
    loading.value = true
    try {
        const params = {
            current: currentPage.value,
            size: pageSize.value,
            typeId: searchForm.value.typeId,
            fName: searchForm.value.fName || undefined,
            stockStatus: searchForm.value.stockStatus || undefined,
            brand: searchForm.value.brand || undefined
        }
        Object.keys(params).forEach(key => {
            if (params[key] === null || params[key] === '') delete params[key]
        })

        const res = await getFurnitureList(params)
        if (res.success || res.code === 200) {
            furnitureList.value = res.data.records || []
            total.value = res.data.total || 0
        }
    } catch (error) {
        ElMessage.error('加载失败')
    } finally {
        loading.value = false
    }
}

// 图片上传处理 - 参考 useProfile.js 的 handleAvatarUpload
const handleImageChange = async (file) => {
    if (!file) return

    // 文件大小校验（2MB）
    if (file.size > 2 * 1024 * 1024) {
        ElMessage.error('图片不能超过2MB')
        return false
    }

    uploading.value = true
    try {
        const res = await uploadFurnitureImage(file.raw)
        if (res.success || res.code === 200) {
            const imageUrl = res.data
            form.fIcon = imageUrl
            ElMessage.success('图片上传成功')
            return true
        } else {
            ElMessage.error(res.message || res.errorMsg || '上传失败')
            return false
        }
    } catch (error) {
        ElMessage.error(error.message || '上传出错')
        return false
    } finally {
        uploading.value = false
    }
}

// 多图片上传
const triggerImageUpload = () => {
  multiImageInput.value?.click()
}

const onMultiImageChange = async (e) => {
  const file = e.target.files[0]
  if (!file) return
  if (file.size > 2 * 1024 * 1024) {
    ElMessage.error('图片不能超过2MB')
    e.target.value = ''
    return
  }
  uploadingImages.value = true
  try {
    const res = await uploadFurnitureImage(file)
    if (res.success || res.code === 200) {
      const url = res.data
      const list = [...imagesList.value, url]
      form.images = list.join(',')
    } else {
      ElMessage.error(res.message || res.errorMsg || '上传失败')
    }
  } catch (err) {
    ElMessage.error('上传出错')
  } finally {
    uploadingImages.value = false
    e.target.value = ''
  }
}

const removeImage = (idx) => {
  const list = [...imagesList.value]
  list.splice(idx, 1)
  form.images = list.join(',')
}

// 搜索与重置
const handleSearch = () => { currentPage.value = 1; loadData() }
const resetSearch = () => {
    searchForm.value = { typeId: null, fName: '', stockStatus: '', brand: '' }
    handleSearch()
}

// 打开新增弹窗
const handleAdd = () => {
    isEdit.value = false
  Object.assign(form, {
    id: null,
    fName: '',
    fIcon: '',
    images: '',
    typeId: null,
    price: 0,
    brand: '',
    stock: 0,
    intro: '',
    description: ''
  })
    dialogVisible.value = true
}

// 打开编辑弹窗
const handleEdit = (row) => {
    isEdit.value = true
    Object.assign(form, row)
    dialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
    if (!formRef.value) return

  // 库存为 0 时二次确认
  if (form.stock === 0) {
    try {
      await ElMessageBox.confirm('库存设置为 0 意味着该商品暂不可售，确认继续？', '库存确认', {
        confirmButtonText: '确认设置为 0',
        cancelButtonText: '取消',
        type: 'warning'
      })
    } catch {
      return
    }
  }

    await formRef.value.validate(async (valid) => {
        if (valid) {
            submitLoading.value = true
            try {
                const res = isEdit.value
                    ? await editFurniture(form)
                    : await addFurniture(form)

                if (res.success || res.code === 200) {
                    ElMessage.success(isEdit.value ? '修改成功' : '添加成功')
                    dialogVisible.value = false
                    loadData()
                } else {
                    ElMessage.error(res.errorMsg || '操作失败')
                }
            } catch (error) {
                ElMessage.error('操作异常')
            } finally {
                submitLoading.value = false
            }
        }
    })
}

// 删除
const handleDelete = (row) => {
    ElMessageBox.confirm(`确定删除家具 "${row.fName}" 吗？`, '提示', { type: 'warning' })
        .then(async () => {
            try {
                const res = await deleteFurniture(row.id)
                if (res.success || res.code === 200) {
                    ElMessage.success('删除成功')
                    loadData()
                } else {
                    ElMessage.error(res.errorMsg || '删除失败')
                }
            } catch (error) {
                ElMessage.error('删除异常')
            }
        })
        .catch(() => { })
}

const getStockType = (stock) => {
    if (stock === 0) return 'danger'
    if (stock < 10) return 'warning'
    return 'success'
}

const getTypeName = (typeId) => {
    const type = typeList.value.find(t => t.id === typeId)
    return type?.name || '-'
}

const handleSizeChange = (val) => {
    currentPage.value = 1
    loadData()
}

// ========== 规格管理方法 ==========

const skuTotalStock = computed(() => skuTableData.value.reduce((sum, s) => sum + (s.stock || 0), 0))
const skuPriceRange = computed(() => {
  if (skuTableData.value.length === 0) return '-'
  const prices = skuTableData.value.map(s => s.price).filter(p => p > 0)
  if (prices.length === 0) return '-'
  const min = Math.min(...prices).toFixed(2)
  const max = Math.max(...prices).toFixed(2)
  return min === max ? `¥${min}` : `¥${min} ~ ¥${max}`
})

const openSpecDialog = async (row) => {
  specFurnitureId.value = row.id
  specFurnitureName.value = row.fName
  specActiveTab.value = 'spec'
  specSaving.value = false
  // 加载已有规格数据
  try {
    const res = await getSpecAndSku(row.id)
    if ((res.success || res.code === 200) && res.data) {
      const data = res.data
      // 填充规格组
      specGroups.value = (data.specGroups || []).map(g => ({
        id: g.id,
        groupName: g.groupName,
        sort: g.sort || 0,
        values: (g.values || []).map(v => ({
          id: v.id,
          valueName: v.valueName,
          valueImage: v.valueImage || '',
          sort: v.sort || 0
        }))
      }))
      // 填充SKU
      skuTableData.value = (data.skuList || []).map(s => ({
        id: s.id,
        skuCode: s.skuCode || '',
        price: Number(s.price) || 0,
        stock: s.stock || 0,
        skuImage: s.skuImage || '',
        status: s.status != null ? s.status : 1,
        specValueIds: [],
        _specText: s.specText || ''
      }))
      // 为已有SKU回填specValueIds
      if (data.skuList) {
        data.skuList.forEach((s, idx) => {
          if (s.specMap && specGroups.value.length > 0) {
            const ids = []
            specGroups.value.forEach((g, gIdx) => {
              const matchVal = g.values.find(v => v.valueName === s.specMap[g.groupName])
              if (matchVal) ids.push(matchVal.id)
            })
            if (idx < skuTableData.value.length) {
              skuTableData.value[idx].specValueIds = ids
            }
          }
        })
      }
    } else {
      specGroups.value = []
      skuTableData.value = []
    }
  } catch (e) {
    specGroups.value = []
    skuTableData.value = []
  }
  specDialogVisible.value = true
}

const onSpecDialogClose = () => {
  specGroups.value = []
  skuTableData.value = []
}

const addSpecGroup = () => {
  specGroups.value.push({
    id: null,
    groupName: '',
    sort: specGroups.value.length,
    values: [{id: null, valueName: '', valueImage: '', sort: 0}]
  })
}

const removeSpecGroup = (gIdx) => {
  specGroups.value.splice(gIdx, 1)
}

const addSpecValue = (gIdx) => {
  const group = specGroups.value[gIdx]
  group.values.push({
    id: null,
    valueName: '',
    valueImage: '',
    sort: group.values.length
  })
}

const removeSpecValue = (gIdx, vIdx) => {
  specGroups.value[gIdx].values.splice(vIdx, 1)
}

// 根据规格组合生成SKU表格
const generateSkuTable = () => {
  const validGroups = specGroups.value.filter(g => g.groupName && g.values.some(v => v.valueName))
  if (validGroups.length === 0) {
    ElMessage.warning('请先填写完整的规格组和规格值')
    return
  }
  // 笛卡尔积
  const combos = validGroups.reduce((acc, group) => {
    const validValues = group.values.filter(v => v.valueName)
    if (acc.length === 0) return validValues.map(v => [v])
    const result = []
    acc.forEach(combo => {
      validValues.forEach(v => {
        result.push([...combo, v])
      })
    })
    return result
  }, [])
  // 保留已有SKU数据（按specValueIds匹配）
  const oldSkuMap = {}
  skuTableData.value.forEach(s => {
    const key = (s.specValueIds || []).sort().join(',')
    if (key) oldSkuMap[key] = s
  })
  skuTableData.value = combos.map(combo => {
    const valueIds = combo.map(v => v.id).sort()
    const key = valueIds.join(',')
    const existing = oldSkuMap[key]
    return {
      id: existing ? existing.id : null,
      skuCode: existing ? existing.skuCode : '',
      price: existing ? existing.price : 0,
      stock: existing ? existing.stock : 0,
      skuImage: existing ? existing.skuImage : '',
      status: existing ? existing.status : 1,
      specValueIds: combo.map(v => v.id),
      _specText: combo.map(v => v.valueName).join(' / ')
    }
  })
  ElMessage.success(`已生成 ${combos.length} 个SKU组合`)
}

const addManualSku = () => {
  skuTableData.value.push({
    id: null,
    skuCode: '',
    price: 0,
    stock: 0,
    skuImage: '',
    status: 1,
    specValueIds: [],
    _specText: ''
  })
}

const removeSku = (idx) => {
  skuTableData.value.splice(idx, 1)
}

const handleSkuImageChange = async (file, row) => {
  if (!file) return
  if (file.size > 2 * 1024 * 1024) {
    ElMessage.error('图片不能超过2MB')
    return
  }
  try {
    const res = await uploadFurnitureImage(file.raw)
    if (res.success || res.code === 200) {
      row.skuImage = res.data
    } else {
      ElMessage.error('上传失败')
    }
  } catch (e) {
    ElMessage.error('上传出错')
  }
}

const handleSaveSpec = async () => {
  // 校验规格组
  for (let i = 0; i < specGroups.value.length; i++) {
    const g = specGroups.value[i]
    if (!g.groupName) {
      ElMessage.warning(`请填写第 ${i + 1} 个规格组的名称`)
      return
    }
    const validVals = g.values.filter(v => v.valueName)
    if (validVals.length === 0) {
      ElMessage.warning(`规格组 "${g.groupName}" 至少需要一个规格值`)
      return
    }
  }
  // 校验SKU
  for (let i = 0; i < skuTableData.value.length; i++) {
    const s = skuTableData.value[i]
    if (!s.price || s.price <= 0) {
      ElMessage.warning(`第 ${i + 1} 个SKU的价格必须大于0`)
      return
    }
  }
  specSaving.value = true
  try {
    const dto = {
      furnitureId: specFurnitureId.value,
      specGroups: specGroups.value.map(g => ({
        id: g.id,
        groupName: g.groupName,
        sort: g.sort,
        values: g.values.filter(v => v.valueName).map(v => ({
          id: v.id,
          valueName: v.valueName,
          valueImage: v.valueImage,
          sort: v.sort
        }))
      })),
      skuList: skuTableData.value.map(s => ({
        id: s.id,
        skuCode: s.skuCode,
        price: s.price,
        stock: s.stock,
        skuImage: s.skuImage,
        status: s.status,
        specValueIds: s.specValueIds
      }))
    }
    const res = await saveSpecAndSku(dto)
    if (res.success || res.code === 200) {
      ElMessage.success('规格保存成功')
      specDialogVisible.value = false
      loadData()
    } else {
      ElMessage.error(res.errorMsg || res.message || '保存失败')
    }
  } catch (e) {
    ElMessage.error('保存异常')
  } finally {
    specSaving.value = false
  }
}

onMounted(() => {
    loadTypes()
    loadData()
})
</script>

<style scoped>
.manage-page {
    background: #fff;
    border-radius: 8px;
    padding: 24px;
    min-height: calc(100vh - 108px);
}

.page-title {
    margin: 0 0 24px 0;
    font-size: 20px;
    color: #333;
}

.search-bar {
    margin-bottom: 20px;
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 10px;
}

.table-img {
    width: 50px;
    height: 50px;
    object-fit: cover;
    border-radius: 4px;
}

.pagination {
    margin-top: 20px;
    display: flex;
    justify-content: center;
}

/* 图片上传样式 */
.upload-container {
    display: flex;
    align-items: center;
    gap: 15px;
}

.avatar-uploader {
    border: 1px dashed var(--el-border-color);
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: var(--el-transition-duration-fast);
}

.avatar-uploader:hover {
    border-color: var(--el-color-primary);
}

.avatar-uploader-icon {
    font-size: 28px;
    color: #8c939d;
    width: 120px;
    height: 120px;
    text-align: center;
    line-height: 120px;
}

.avatar {
    width: 120px;
    height: 120px;
    display: block;
    object-fit: cover;
}

.upload-tip {
    display: flex;
    flex-direction: column;
    gap: 5px;
}

.tip-text {
    font-size: 12px;
    color: #606266;
}

/* 多图片上传 */
.images-upload-area {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.image-thumb {
  position: relative;
  width: 80px;
  height: 80px;
  border-radius: 4px;
  overflow: hidden;
  border: 1px solid #dcdfe6;
}

.thumb-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.thumb-delete {
  position: absolute;
  top: 2px;
  right: 2px;
  width: 18px;
  height: 18px;
  background: rgba(0, 0, 0, 0.55);
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  cursor: pointer;
  line-height: 1;
}

.thumb-delete:hover {
  background: #f56c6c;
}

.image-add-btn {
  width: 80px;
  height: 80px;
  border: 1px dashed #dcdfe6;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #8c939d;
  cursor: pointer;
  transition: all 0.2s;
}

.image-add-btn:hover {
  border-color: #409eff;
  color: #409eff;
}

.form-item-tip {
  font-size: 12px;
  color: #909399;
}

/* 规格管理弹窗 */
.spec-empty-tip {
  text-align: center;
  padding: 20px;
  color: #999;
}

.spec-group-card {
  background: #f8f9fa;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  padding: 14px 16px;
  margin-bottom: 12px;
}

.spec-group-header {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.spec-values-area {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.spec-value-chip {
  display: flex;
  align-items: center;
  gap: 2px;
  background: white;
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 2px;
}

.sku-actions {
  display: flex;
  gap: 10px;
}

.sku-summary {
  margin-top: 12px;
  padding: 10px;
  background: #f0f5f3;
  border-radius: 6px;
  font-size: 13px;
  color: #3e4e49;
}

.sku-img-uploader {
  width: 40px;
  height: 40px;
  border: 1px dashed #ddd;
  border-radius: 4px;
  cursor: pointer;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.sku-img-thumb {
  width: 40px;
  height: 40px;
  object-fit: cover;
}

.sku-img-add {
  font-size: 18px;
  color: #999;
}
</style>