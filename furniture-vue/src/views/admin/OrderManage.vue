<template>
  <div class="manage-page">
    <h2 class="page-title">📋 订单管理</h2>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-select v-model="searchForm.status" placeholder="订单状态" clearable style="width: 150px; margin-left: 10px">
        <el-option label="待支付" :value="0"/>
        <el-option label="已支付" :value="1"/>
        <el-option label="已发货" :value="2"/>
        <el-option label="已完成" :value="3"/>
        <el-option label="已取消" :value="4"/>
        <el-option label="已评价" :value="5"/>
      </el-select>
      <el-input v-model="searchForm.phone" placeholder="收货手机号" clearable
                style="width: 150px; margin-left: 10px"/>
      <el-input v-model="searchForm.consignee" placeholder="收货人姓名" clearable
                style="width: 150px; margin-left: 10px"/>
      <el-button type="primary" style="margin-left: 10px" @click="handleSearch">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
      <el-button type="success" style="margin-left: 20px" @click="handleExport">导出 Excel</el-button>
    </div>

    <!-- 表格 -->
    <el-table :data="orderList" v-loading="loading" border>
      <el-table-column prop="id" label="订单号" min-width="180"/>
      <el-table-column prop="consignee" label="收货人" width="100"/>
      <el-table-column prop="phone" label="手机号" width="130"/>
      <el-table-column prop="address" label="收货地址" min-width="200" show-overflow-tooltip/>
      <el-table-column prop="totalPrice" label="金额" width="120">
        <template #default="{ row }">
          <span style="color: #d95a5a; font-weight: 600">¥{{ row.totalPrice }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" width="150">
        <template #default="{ row }">
          <span v-if="row.remark">{{ row.remark }}</span>
          <el-tag v-else type="info" size="small">无备注</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="商品详情" width="180">
        <template #default="{ row }">
          <el-tag type="primary" size="small">
            共 {{ row.itemList?.length || 0 }} 件
          </el-tag>
          <el-button size="small" type="primary" text style="margin-left: 10px" @click="handleViewItems(row)">
            查看明细
          </el-button>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180"/>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <!-- 已支付状态显示发货按钮 -->
          <el-button v-if="row.status === 1" type="primary" size="small" @click="handleShip(row)">
            发货
          </el-button>
          <!-- 其他状态显示 "-" -->
          <span v-else style="color: #909399;">-</span>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize"
                     :page-sizes="[10, 20, 50, 100]" :total="total" layout="total, sizes, prev, pager, next, jumper"
                     @size-change="handleSizeChange" @current-change="loadData"/>
    </div>

    <!-- 商品明细弹窗 -->
    <el-dialog v-model="dialogVisible" title="🛒 商品明细" width="750px">
      <el-table :data="currentOrderItems" border size="small">
        <el-table-column prop="furnitureName" label="商品名称" min-width="130"/>
        <el-table-column label="规格" width="160">
          <template #default="{ row }">
            <span v-if="row.skuSpec" style="font-size: 12px; color: #666;">{{ row.skuSpec }}</span>
            <el-tag v-else type="info" size="small">默认规格</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="单价" width="90">
          <template #default="{ row }">
            ¥{{ row.price }}
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="70"/>
        <el-table-column label="小计" width="100">
          <template #default="{ row }">
            ¥{{ row.itemTotalPrice }}
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import {onMounted, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {exportOrders, getOrderList, shipOrder} from '@/api/admin/order.js'

const loading = ref(false)
const orderList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)

const handleExport = async () => {
  try {
    const blob = await exportOrders()
    const url = window.URL.createObjectURL(new Blob([blob], {type: 'text/csv;charset=UTF-8'}))
    const a = document.createElement('a')
    a.href = url
    a.download = 'orders.csv'
    a.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (e) {
    ElMessage.error('导出失败，请重试')
  }
}
const total = ref(0)

const dialogVisible = ref(false)
const currentOrderItems = ref([])

const searchForm = ref({
  userId: null,
  status: null,
  phone: '',
  consignee: ''
})

const statusMap = {
  0: {text: '待支付', type: 'warning'},
  1: {text: '已支付', type: 'success'},
  2: {text: '已发货', type: 'primary'},
  3: {text: '已完成', type: 'info'},
  4: {text: '已取消', type: 'danger'},
  5: {text: '已评价', type: 'success'}
}

const getStatusText = (status) => statusMap[status]?.text || '未知'
const getStatusType = (status) => statusMap[status]?.type || 'info'

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      current: currentPage.value,
      size: pageSize.value,
      userId: searchForm.value.userId || undefined,
      status: searchForm.value.status,
      phone: searchForm.value.phone || undefined,
      consignee: searchForm.value.consignee || undefined
    }
    Object.keys(params).forEach(key => {
      if (params[key] === null || params[key] === '') delete params[key]
    })

    const res = await getOrderList(params)
    if (res.success || res.code === 200) {
      orderList.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadData()
}

const resetSearch = () => {
  searchForm.value = {userId: null, status: null, phone: '', consignee: ''}
  handleSearch()
}

// 发货处理
const handleShip = async (row) => {
  try {
    await ElMessageBox.confirm(
        `确定对订单 "${row.id}" 进行发货吗？`,
        '确认发货',
        {
          confirmButtonText: '确定发货',
          cancelButtonText: '取消',
          type: 'warning'
        }
    )

    const res = await shipOrder(row.id)
    if (res.success || res.code === 200) {
      ElMessage.success('发货成功')
      loadData() // 刷新列表
    } else {
      ElMessage.error(res.errorMsg || '发货失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
      ElMessage.error('发货异常')
    }
  }
}

const handleViewItems = (row) => {
  currentOrderItems.value = row.itemList || []
  dialogVisible.value = true
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
</style>