<template>
  <div class="profile-review-manage">
    <div class="page-header">
      <h2>用户资料审核</h2>
      <p class="page-desc">审核用户修改的昵称和头像，维护社区内容安全</p>
    </div>

    <!-- Tab 切换 -->
    <el-tabs v-model="activeTab" @tab-change="onTabChange">
      <el-tab-pane label="昵称审核" name="nickname" />
      <el-tab-pane label="头像审核" name="avatar" />
    </el-tabs>

    <!-- 表格 -->
    <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
      <el-table-column label="用户昵称" min-width="1">
        <template #default="{ row }">
          <span>{{ row.currentNickname || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="activeTab === 'nickname' ? '修改后昵称' : '修改后头像'" min-width="1">
        <template #default="{ row }">
          <template v-if="activeTab === 'nickname'">
            <span class="pending-value">{{ row.pendingNickname }}</span>
            <el-tag v-if="row.nicknameReviewStatus === 3" type="warning" size="small" style="margin-left: 8px">
              AI待复审
            </el-tag>
          </template>
          <template v-else>
            <el-image
              v-if="row.pendingIcon"
              :src="row.pendingIcon"
              style="width: 100px; height: 100px; border-radius: 4px; border: 1px solid #eee; cursor: pointer"
              fit="cover"
              :preview-src-list="[row.pendingIcon]"
              :preview-teleported="true"
            />
            <span v-else class="text-muted">无</span>
          </template>
        </template>
      </el-table-column>
      <el-table-column label="操作" min-width="1" align="center">
        <template #default="{ row }">
          <el-button type="success" size="small" @click="approve(row)">通过</el-button>
          <el-button type="danger" size="small" @click="handleReject(row)">拒绝</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-wrap">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchData"
        @current-change="fetchData"
      />
    </div>

    <!-- 拒绝原因弹窗 -->
    <el-dialog v-model="rejectDialog.visible" title="拒绝原因" width="420px">
      <el-input
        v-model="rejectDialog.reason"
        type="textarea"
        :rows="3"
        placeholder="请输入拒绝原因（选填）"
      />
      <template #footer>
        <el-button @click="rejectDialog.visible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject">确认拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  getPendingProfileReviews,
  approveNickname,
  rejectNickname,
  approveIcon,
  rejectIcon,
} from "@/api/admin/profileReview.js";
import { logger } from "@/utils/logger.js";

const loading = ref(false);
const tableData = ref([]);
const page = ref(1);
const pageSize = ref(10);
const total = ref(0);
const activeTab = ref("nickname");

const rejectDialog = reactive({
  visible: false,
  reason: "",
  currentRow: null,
});

const fetchData = async () => {
  loading.value = true;
  try {
    const res = await getPendingProfileReviews(page.value, pageSize.value, activeTab.value);
    if (res.success || res.code === 200) {
      tableData.value = res.data?.records || [];
      total.value = res.data?.total || 0;
    }
  } catch (e) {
    logger.error(e);
  } finally {
    loading.value = false;
  }
};

const onTabChange = () => {
  page.value = 1;
  fetchData();
};

const approve = (row) => {
  const msg = activeTab.value === "nickname"
    ? "确认通过「" + row.pendingNickname + "」？"
    : "确认通过「" + row.currentNickname + "」的头像？";
  ElMessageBox.confirm(msg, "确认审核", { type: "info" }).then(async () => {
    try {
      const fn = activeTab.value === "nickname" ? approveNickname : approveIcon;
      const res = await fn(row.userId);
      if (res.success || res.code === 200) {
        ElMessage.success("审核通过");
        fetchData();
      }
    } catch (e) {
      logger.error(e);
    }
  }).catch(() => {});
};

const handleReject = (row) => {
  rejectDialog.currentRow = row;
  rejectDialog.reason = "";
  rejectDialog.visible = true;
};

const confirmReject = async () => {
  const row = rejectDialog.currentRow;
  if (!row) return;
  try {
    const fn = activeTab.value === "nickname" ? rejectNickname : rejectIcon;
    const res = await fn(row.userId, rejectDialog.reason);
    if (res.success || res.code === 200) {
      ElMessage.success("已拒绝");
      rejectDialog.visible = false;
      fetchData();
    }
  } catch (e) {
    logger.error(e);
  }
};

onMounted(() => {
  fetchData();
});
</script>

<style scoped>
.profile-review-manage {
  padding: 6px 0;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 6px 0;
  font-size: 20px;
  color: #333;
  font-weight: 600;
}

.page-desc {
  margin: 0;
  color: #999;
  font-size: 13px;
}

.current-value {
  color: #888;
}

.pending-value {
  color: #333;
  font-weight: 500;
}

.text-muted {
  color: #bbb;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 16px;
  padding-top: 12px;
}
</style>