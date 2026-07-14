<template>
  <div class="address-page-new">
    <div class="page-breadcrumb">
      <div class="breadcrumb-left">
        <router-link to="/">首页</router-link>
        <span>/</span>
        <router-link to="/user/profile">个人中心</router-link>
        <span>/</span>
        <span class="current">收货地址</span>
      </div>
      <el-button
        type="primary"
        size="small"
        @click="openAddDialog"
        class="add-addr-btn"
      >
        <el-icon><Plus /></el-icon>
        新增地址
      </el-button>
    </div>
    <div class="address-container">
      <div v-if="loading" class="loading">加载中...</div>

      <template v-else>
        <div v-if="addressList.length === 0" class="empty">
          <el-empty description="暂无收货地址">
            <el-button type="primary" @click="openAddDialog"
              >添加新地址</el-button
            >
          </el-empty>
        </div>

        <div v-else class="address-list">
          <div
            v-for="addr in addressList"
            :key="addr.id"
            class="address-card"
            :class="{ default: addr.isDefault === 1 }"
          >
            <div class="address-info">
              <div class="address-header">
                <span class="consignee">{{ addr.consignee }}</span>
                <span class="phone">{{ addr.phone }}</span>
                <el-tag
                  v-if="addr.isDefault === 1"
                  type="danger"
                  size="small"
                  effect="dark"
                  >默认</el-tag
                >
              </div>
              <div class="address-detail">{{ addr.address }}</div>
            </div>
            <div class="address-actions">
              <el-button
                v-if="addr.isDefault !== 1"
                text
                type="primary"
                size="small"
                @click="handleSetDefault(addr.id)"
                >设为默认
              </el-button>
              <el-button
                text
                type="primary"
                size="small"
                @click="openEditDialog(addr)"
                >编辑</el-button
              >
              <el-button
                text
                type="danger"
                size="small"
                @click="handleDelete(addr)"
                >删除</el-button
              >
            </div>
          </div>
        </div>
      </template>
    </div>

    <!-- 新增/编辑地址弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editMode ? '编辑地址' : '新增地址'"
      width="480px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="收货人" prop="consignee">
          <el-input
            v-model="form.consignee"
            placeholder="请输入收货人姓名"
            maxlength="20"
          />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input
            v-model="form.phone"
            placeholder="请输入手机号"
            maxlength="11"
          />
        </el-form-item>
        <el-form-item label="详细地址" prop="address">
          <el-input
            v-model="form.address"
            type="textarea"
            :rows="3"
            placeholder="省/市/区 + 街道门牌号"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="设为默认">
          <el-switch v-model="form.isDefaultBool" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting"
          >确定</el-button
        >
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { ArrowLeft, Plus } from "@element-plus/icons-vue";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  deleteAddress,
  getAddressList,
  saveAddress,
  setDefaultAddress,
} from "@/api/address.js";
import { logger } from "@/utils/logger.js";

const router = useRouter();
const loading = ref(false);
const submitting = ref(false);
const addressList = ref([]);
const dialogVisible = ref(false);
const editMode = ref(false);
const editId = ref(null);
const formRef = ref(null);

const form = reactive({
  consignee: "",
  phone: "",
  address: "",
  isDefaultBool: false,
});

const rules = {
  consignee: [{ required: true, message: "请输入收货人姓名", trigger: "blur" }],
  phone: [
    { required: true, message: "请输入手机号", trigger: "blur" },
    {
      pattern: /^1([38][0-9]|4[579]|5[0-35-9]|6[2567]|7[0-8]|9[0-9])\d{8}$/,
      message: "手机号格式不正确",
      trigger: "blur",
    },
  ],
  address: [{ required: true, message: "请输入详细地址", trigger: "blur" }],
};

const loadAddresses = async () => {
  loading.value = true;
  try {
    const res = await getAddressList();
    if (res.success || res.code === 200) {
      addressList.value = res.data || [];
    }
  } catch (e) {
    logger.error(e);
  } finally {
    loading.value = false;
  }
};

const openAddDialog = () => {
  editMode.value = false;
  editId.value = null;
  form.consignee = "";
  form.phone = "";
  form.address = "";
  form.isDefaultBool = addressList.value.length === 0;
  dialogVisible.value = true;
};

const openEditDialog = (addr) => {
  editMode.value = true;
  editId.value = addr.id;
  form.consignee = addr.consignee;
  form.phone = addr.phone;
  form.address = addr.address;
  form.isDefaultBool = addr.isDefault === 1;
  dialogVisible.value = true;
};

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) return;

  submitting.value = true;
  try {
    const data = {
      id: editId.value || undefined,
      consignee: form.consignee,
      phone: form.phone,
      address: form.address,
      isDefault: form.isDefaultBool ? 1 : 0,
    };
    const res = await saveAddress(data);
    if (res.success || res.code === 200) {
      ElMessage.success(editMode.value ? "修改成功" : "添加成功");
      dialogVisible.value = false;
      loadAddresses();
    }
  } catch (e) {
    logger.error("操作失败:", e);
  } finally {
    submitting.value = false;
  }
};

const handleDelete = (addr) => {
  ElMessageBox.confirm("确定要删除这个地址吗？", "删除确认", {
    type: "warning",
    confirmButtonText: "确定删除",
    cancelButtonText: "取消",
  })
    .then(async () => {
      const res = await deleteAddress(addr.id);
      if (res.success || res.code === 200) {
        ElMessage.success("删除成功");
        loadAddresses();
      }
    })
    .catch(() => {});
};

const handleSetDefault = async (id) => {
  try {
    const res = await setDefaultAddress(id);
    if (res.success || res.code === 200) {
      ElMessage.success("设置成功");
      loadAddresses();
    }
  } catch (e) {
    logger.error(e);
  }
};

const goBack = () => router.back();

onMounted(loadAddresses);
</script>

<style scoped>
.address-page-new .address-container {
  max-width: 800px;
  margin: 20px auto;
  padding: 0 16px;
}

.loading {
  text-align: center;
  padding: 60px 0;
  color: #999;
}

.address-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.address-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border: 1px solid #eee;
  transition: all 0.2s;
}

.address-card:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.address-card.default {
  border-color: #5a6a7a;
  background: #fafbfc;
}

.address-info {
  flex: 1;
  min-width: 0;
}

.address-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.consignee {
  font-weight: 600;
  font-size: 15px;
  color: #333;
}

.phone {
  color: #666;
  font-size: 14px;
}

.address-detail {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
}

.address-actions {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
  margin-left: 16px;
}
.page-breadcrumb {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: var(--space-4) var(--space-6);
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: var(--text-xs);
  color: var(--color-text-tertiary);
}
.page-breadcrumb a {
  color: var(--color-text-tertiary);
  text-decoration: none;
}
.page-breadcrumb a:hover {
  color: var(--color-text-primary);
}
.page-breadcrumb .current {
  color: var(--color-text-primary);
}
.breadcrumb-left {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}
.add-addr-btn {
  flex-shrink: 0;
}
</style>
