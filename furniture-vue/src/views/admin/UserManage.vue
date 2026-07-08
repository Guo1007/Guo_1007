<template>
  <div class="manage-page">
    <h2 class="page-title">👥 用户管理</h2>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input v-model="searchForm.phone" placeholder="手机号搜索" clearable style="width: 180px"
                @keyup.enter="handleSearch"/>
      <el-input v-model="searchForm.email" placeholder="邮箱搜索" clearable style="width: 200px; margin-left: 10px"
                @keyup.enter="handleSearch"/>
      <el-select v-model="searchForm.isAdmin" placeholder="用户类型" clearable
                 style="width: 150px; margin-left: 10px">
        <el-option label="普通用户" :value="0"/>
        <el-option label="管理员" :value="1"/>
      </el-select>
      <el-button type="primary" style="margin-left: 10px" @click="handleSearch">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <el-table :data="userList" v-loading="loading" border>
      <el-table-column prop="userName" label="用户名" min-width="100"/>
      <el-table-column prop="phone" label="手机号" width="130"/>
      <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip/>
      <el-table-column prop="address" label="收货地址" min-width="300" show-overflow-tooltip/>
      <el-table-column prop="isAdmin" label="类型" width="100">
        <template #default="{ row }">
          <el-tag :type="row.isAdmin === 1 ? 'danger' : 'info'">
            {{ row.isAdmin === 1 ? '管理员' : '普通用户' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册时间" width="180"/>
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
                     @size-change="handleSizeChange" @current-change="loadData"/>
    </div>

    <!-- 编辑用户弹窗 -->
    <el-dialog v-model="dialogVisible" title="编辑用户" width="450px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="用户ID">
          <el-input v-model="form.id" disabled/>
        </el-form-item>

        <el-form-item label="用户名">
          <el-input v-model="form.userName" disabled/>
        </el-form-item>

        <el-form-item label="手机号">
          <el-input v-model="form.phone" disabled/>
        </el-form-item>

        <el-form-item label="邮箱">
          <el-input v-model="form.email" disabled/>
        </el-form-item>

        <el-form-item label="用户类型" prop="isAdmin">
          <el-radio-group v-model="form.isAdmin">
            <el-radio :label="0">普通用户</el-radio>
            <el-radio :label="1">管理员</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="重置密码">
          <el-input v-model="form.newPassword" type="password" placeholder="不填则不重置密码" show-password/>
          <div class="form-tip">留空表示不修改密码，填写则重置为新密码</div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {deleteUser, editUser, getUserList} from '@/api/admin/user.js'

const loading = ref(false)
const submitting = ref(false)
const userList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const formRef = ref(null)

const searchForm = ref({
  phone: '',
  email: '',
  isAdmin: null
})

// 编辑表单
const form = reactive({
  id: null,
  userName: '',
  phone: '',
  email: '',
  isAdmin: 0,
  newPassword: ''
})

// 表单校验规则
const rules = {
  isAdmin: [{required: true, message: '请选择用户类型', trigger: 'change'}]
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      current: currentPage.value,
      size: pageSize.value,
      phone: searchForm.value.phone || undefined,
      email: searchForm.value.email || undefined,
      isAdmin: searchForm.value.isAdmin
    }
    Object.keys(params).forEach(key => {
      if (params[key] === null || params[key] === '') delete params[key]
    })

    const res = await getUserList(params)
    if (res.success || res.code === 200) {
      userList.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('加载失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索与重置
const handleSearch = () => {
  currentPage.value = 1
  loadData()
}

const resetSearch = () => {
  searchForm.value = {phone: '', email: '', isAdmin: null}
  handleSearch()
}

// 打开编辑弹窗
const handleEdit = (row) => {
  // 填充表单
  form.id = row.id
  form.userName = row.userName
  form.phone = row.phone
  form.email = row.email || ''
  form.isAdmin = row.isAdmin
  form.newPassword = ''  // 密码始终为空，需要管理员手动输入

  dialogVisible.value = true
}

// 提交编辑
const submitEdit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    // 确认提示
    const confirmMsg = form.newPassword
        ? `确定修改用户类型并重置密码为"${form.newPassword}"吗？`
        : '确定修改用户类型吗？（密码不变）'

    try {
      await ElMessageBox.confirm(confirmMsg, '确认修改', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
    } catch {
      return  // 用户取消
    }

    submitting.value = true
    try {
      // 构建请求数据
      const data = {
        id: form.id,
        isAdmin: form.isAdmin
      }
      // 只有填写了密码才传递
      if (form.newPassword && form.newPassword.trim()) {
        data.newPassword = form.newPassword.trim()
      }

      const res = await editUser(data)
      if (res.success || res.code === 200) {
        ElMessage.success('修改成功')
        dialogVisible.value = false
        loadData()
      } else {
        ElMessage.error(res.msg || '修改失败')
      }
    } catch (error) {
      console.error('修改异常:', error)
    } finally {
      submitting.value = false
    }
  })
}

// 删除用户
const handleDelete = (row) => {
  ElMessageBox.confirm(
      `确定删除用户 "${row.userName}"（${row.phone}）吗？此操作不可恢复！`,
      '删除确认',
      {
        type: 'error',
        confirmButtonText: '确定删除',
        cancelButtonText: '取消'
      }
  )
      .then(async () => {
        try {
          const res = await deleteUser(row.id)
          if (res.success || res.code === 200) {
            ElMessage.success('删除成功')
            loadData()
          } else {
            ElMessage.error(res.msg || '删除失败')
          }
        } catch (error) {
          console.error('删除异常:', error)
        }
      })
      .catch(() => {
      })
}

const handleSizeChange = (val) => {
  currentPage.value = 1
  loadData()
}

onMounted(loadData)
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

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}
</style>