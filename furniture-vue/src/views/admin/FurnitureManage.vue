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
            <el-table-column label="操作" width="150" fixed="right">
                <template #default="{ row }">
                    <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
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
                    <el-input v-model="form.fIcon" placeholder="图片URL" style="margin-top: 10px;" />
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
            </el-form>
            <template #footer>
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getFurnitureList, addFurniture, editFurniture, deleteFurniture, uploadFurnitureImage } from '@/api/admin/furniture.js'
import { getFurnitureTypeList } from '@/api/furniture.js'
import {imgUrl} from '@/utils/img.js'

const loading = ref(false)
const submitLoading = ref(false)
const uploading = ref(false)
const furnitureList = ref([])
const typeList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

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
    typeId: null,
    price: 0,
    brand: '',
    stock: 0,
    intro: ''
})

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

// 搜索与重置
const handleSearch = () => { currentPage.value = 1; loadData() }
const resetSearch = () => {
    searchForm.value = { typeId: null, fName: '', stockStatus: '', brand: '' }
    handleSearch()
}

// 打开新增弹窗
const handleAdd = () => {
    isEdit.value = false
    Object.assign(form, { id: null, fName: '', fIcon: '', typeId: null, price: 0, brand: '', stock: 0, intro: '' })
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
</style>