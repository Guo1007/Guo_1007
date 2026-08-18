<template>
  <div class="profile-review-manage">
    <div class="page-header">
      <h2>头像审核</h2>
      <p class="page-desc">审核用户修改的头像</p>
    </div>

    <el-tabs v-model="activeTab" @tab-change="onTabChange">
      <el-tab-pane label="全部" name="" />
      <el-tab-pane label="待审核" name="1" />
      <el-tab-pane label="已通过" name="0" />
      <el-tab-pane label="已拒绝" name="2" />
    </el-tabs>

    <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
      <el-table-column label="用户昵称" min-width="1">
        <template #default="{ row }">
          <span>{{ row.userName || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="修改后头像" min-width="1">
        <template #default="{ row }">
          <div v-if="row.pendingIcon" class="avatar-preview-cell">
            <el-image
              :src="row.pendingIcon"
              class="avatar-thumb"
              fit="cover"
              :preview-src-list="[row.pendingIcon]"
              :preview-teleported="true"
            />
            <span class="avatar-zoom-hint">点击放大</span>
          </div>
          <span v-else class="text-muted">-</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.reviewStatus === 0" type="success" size="small">已通过</el-tag>
          <el-tag v-else-if="row.reviewStatus === 1" type="warning" size="small">待审核</el-tag>
          <el-tag v-else-if="row.reviewStatus === 2" type="danger" size="small">已拒绝</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" min-width="1" align="center">
        <template #default="{ row }">
          <template v-if="row.reviewStatus === 1">
            <el-button type="success" size="small" @click="approve(row)">通过</el-button>
            <el-button type="danger" size="small" @click="handleReject(row)">拒绝</el-button>
          </template>
          <span v-else class="text-muted">-</span>
        </template>
      </el-table-column>
    </el-table>

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

    <el-dialog v-model="rejectDialog.visible" title="拒绝原因" width="480px">
      <div class="reject-reason-tags">
        <el-tag
          v-for="r in rejectReasons"
          :key="r.id"
          :type="rejectDialog.reason === r.reason ? 'danger' : 'info'"
          style="cursor: pointer; margin: 4px"
          @click="rejectDialog.reason = r.reason"
        >{{ r.reason }}</el-tag>
      </div>
      <el-input
        v-model="rejectDialog.reason"
        type="textarea"
        :rows="2"
        placeholder="或自定义输入拒绝原因（选填）"
        style="margin-top: 12px"
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
  getIconReviewList,
  approveIcon,
  rejectIcon,
} from "@/api/admin/profileReview.js";
import { getAllRejectReasons } from "@/api/admin/rejectReason.js";
import { logger } from "@/utils/logger.js";

const loading = ref(false);
const tableData = ref([]);
const page = ref(1);
const pageSize = ref(10);
const total = ref(0);
const activeTab = ref("");
const rejectReasons = ref([]);

const rejectDialog = reactive({
  visible: false,
  reason: "",
  currentRow: null,
});

const fetchData = async () => {
  loading.value = true;
  try {
    const res = await getIconReviewList(page.value, pageSize.value, activeTab.value || undefined);
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

const loadRejectReasons = async () => {
  try {
    const res = await getAllRejectReasons();
    if (res.success || res.code === 200) {
      rejectReasons.value = res.data || [];
    }
  } catch (e) { /* ignore */ }
};

const onTabChange = () => {
  page.value = 1;
  fetchData();
};

const approve = (row) => {
  ElMessageBox.confirm("确认通过「" + row.userName + "」的头像？", "确认审核", { type: "info" }).then(async () => {
    try {
      const res = await approveIcon(row.userId);
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
    const res = await rejectIcon(row.userId, rejectDialog.reason);
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
  loadRejectReasons();
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

.text-muted {
  color: #bbb;
}

.avatar-preview-cell {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.avatar-thumb {
  width: 48px;
  height: 48px;
  border-radius: 6px;
  border: 1px solid #e8e8e8;
  cursor: pointer;
  flex-shrink: 0;
  transition: box-shadow 0.2s;
}

.avatar-thumb:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
}

.avatar-zoom-hint {
  font-size: 12px;
  color: #999;
  white-space: nowrap;
}

.reject-reason-tags {
  max-height: 200px;
  overflow-y: auto;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 16px;
  padding-top: 12px;
}
</style>