<template>
  <div class="manage-page">
    <h2 class="page-title">📧 通知设置</h2>

    <el-card v-loading="loading">
      <!-- 总开关 -->
      <div class="setting-row">
        <div class="setting-info">
          <p class="setting-title">管理员邮件通知</p>
          <p class="setting-desc">
            开启后，新订单、新退款申请等会通过邮件通知指定的管理员
          </p>
        </div>
        <el-switch v-model="enabled" size="large" />
      </div>

      <el-divider />

      <!-- 接收管理员 -->
      <div class="setting-row">
        <div class="setting-info">
          <p class="setting-title">接收通知的管理员</p>
          <p class="setting-desc">
            勾选需要接收邮件通知的管理员，仅开启状态下生效
          </p>
        </div>
        <div class="setting-control">
          <el-select
            v-model="selectedAdminIds"
            multiple
            collapse-tags
            collapse-tags-tooltip
            placeholder="选择接收通知的管理员"
            style="width: 100%"
          >
            <el-option
              v-for="a in admins"
              :key="a.id"
              :label="a.userName + (a.email ? '（' + a.email + '）' : '')"
              :value="a.id"
              :disabled="!a.email"
            />
          </el-select>
          <p v-if="admins.length === 0" class="empty-tip">
            暂无可选择的管理员（需要 is_admin=1 的账号）
          </p>
        </div>
      </div>

      <div class="save-bar">
        <el-button type="primary" :loading="saving" @click="handleSave">
          保存设置
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { ElMessage } from "element-plus";
import { getNotifySetting, saveNotifySetting } from "@/api/admin/notifySetting.js";

const loading = ref(false);
const saving = ref(false);
const enabled = ref(false);
const admins = ref([]);
const selectedAdminIds = ref([]);

const loadSetting = async () => {
  loading.value = true;
  try {
    const res = await getNotifySetting();
    if (res.success || res.code === 200) {
      enabled.value = !!res.data.enabled;
      selectedAdminIds.value = res.data.adminIds || [];
      admins.value = res.data.admins || [];
    }
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
};

const handleSave = async () => {
  if (enabled.value && selectedAdminIds.value.length === 0) {
    ElMessage.warning("开启通知后请至少选择一位接收管理员");
    return;
  }
  saving.value = true;
  try {
    const res = await saveNotifySetting({
      enabled: enabled.value,
      adminIds: selectedAdminIds.value,
    });
    if (res.success || res.code === 200) {
      ElMessage.success("保存成功");
      loadSetting();
    } else {
      ElMessage.error(res.msg || res.message || "保存失败");
    }
  } catch (e) {
    console.error(e);
    ElMessage.error("保存失败");
  } finally {
    saving.value = false;
  }
};

onMounted(() => {
  loadSetting();
});
</script>

<style scoped>
.manage-page {
  padding: 20px;
}

.page-title {
  margin-bottom: 20px;
  font-size: 20px;
  color: #333;
}

.setting-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 8px 4px;
}

.setting-info {
  flex: 1;
}

.setting-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin: 0 0 4px 0;
}

.setting-desc {
  font-size: 13px;
  color: #999;
  margin: 0;
}

.setting-control {
  width: 400px;
}

.empty-tip {
  font-size: 13px;
  color: #999;
  margin-top: 8px;
}

.save-bar {
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}
</style>
