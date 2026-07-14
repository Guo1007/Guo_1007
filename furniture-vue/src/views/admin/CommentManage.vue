<template>
  <div class="manage-page">
    <h2 class="page-title">💬 评价审核</h2>

    <!-- 待审核提醒 -->
    <el-alert
      v-if="totalPending > 0"
      type="warning"
      show-icon
      :closable="false"
      class="pending-alert"
    >
      <template #title>
        <span
          >共有 <strong>{{ totalPending }}</strong> 条内容待审核（评价
          {{ pendingCounts.commentCount }} 条、追评
          {{ pendingCounts.appendCount }} 条、回复
          {{ pendingCounts.reviewCommentCount }} 条）</span
        >
      </template>
    </el-alert>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <!-- 商品评价 -->
      <el-tab-pane name="comment">
        <template #label>
          商品评价
          <el-badge
            v-if="pendingCounts.commentCount > 0"
            :value="pendingCounts.commentCount"
            class="tab-badge"
          />
        </template>
        <div class="toolbar">
          <el-button
            type="danger"
            :disabled="selectedComments.length === 0"
            @click="handleBatchDeleteComments"
          >
            批量删除
            <span v-if="selectedComments.length > 0"
              >({{ selectedComments.length }})</span
            >
          </el-button>
        </div>
        <el-table
          :data="commentList"
          v-loading="commentLoading"
          border
          @selection-change="(val) => (selectedComments = val)"
        >
          <el-table-column type="selection" width="45" />
          <el-table-column prop="userName" label="用户" width="120" />
          <el-table-column
            prop="goodsName"
            label="商品"
            min-width="150"
            show-overflow-tooltip
          />
          <el-table-column prop="score" label="评分" width="100">
            <template #default="{ row }">
              <span>{{ "⭐".repeat(row.score) }}</span>
            </template>
          </el-table-column>
          <el-table-column
            prop="content"
            label="评价内容"
            min-width="200"
            show-overflow-tooltip
          />
          <el-table-column label="图片" width="120">
            <template #default="{ row }">
              <div
                v-if="parseJson(row.imgUrl).length > 0"
                class="media-preview"
              >
                <img
                  v-for="(img, idx) in parseJson(row.imgUrl).slice(0, 2)"
                  :key="idx"
                  :src="img"
                  class="thumb-img"
                  @click="previewImage(img)"
                />
                <span v-if="parseJson(row.imgUrl).length > 2" class="more-count"
                  >+{{ parseJson(row.imgUrl).length - 2 }}</span
                >
              </div>
              <span v-else class="no-media">-</span>
            </template>
          </el-table-column>
          <el-table-column label="视频" width="100">
            <template #default="{ row }">
              <el-button
                v-if="row.videoUrl"
                type="primary"
                text
                size="small"
                @click="previewVideo(row.videoUrl)"
                >预览
              </el-button>
              <span v-else class="no-media">-</span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.status === 0" type="warning" size="small"
                >待审核</el-tag
              >
              <el-tag v-else-if="row.status === 1" type="success" size="small"
                >已通过</el-tag
              >
              <el-tag v-else type="danger" size="small">已拒绝</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="时间" width="180" />
          <el-table-column label="操作" width="220" fixed="right">
            <template #default="{ row }">
              <template v-if="row.status === 0">
                <el-button-group>
                  <el-button
                    type="success"
                    size="small"
                    @click="handleApproveComment(row)"
                    >通过</el-button
                  >
                  <el-button
                    type="warning"
                    size="small"
                    plain
                    @click="handleRejectComment(row)"
                    >拒绝</el-button
                  >
                </el-button-group>
                <el-button
                  type="danger"
                  size="small"
                  text
                  @click="handleDeleteComment(row.id)"
                  style="margin-left: 4px"
                  >删除</el-button
                >
              </template>
              <template v-else>
                <span style="color: #999; font-size: 12px; margin-right: 8px"
                  >已处理</span
                >
                <el-button
                  type="danger"
                  size="small"
                  text
                  @click="handleDeleteComment(row.id)"
                  >删除</el-button
                >
              </template>
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination">
          <el-pagination
            v-model:current-page="commentPage"
            :page-size="10"
            :total="commentTotal"
            layout="total, prev, pager, next"
            @current-change="loadComments"
          />
        </div>
      </el-tab-pane>

      <!-- 追评 -->
      <el-tab-pane name="append">
        <template #label>
          追评
          <el-badge
            v-if="pendingCounts.appendCount > 0"
            :value="pendingCounts.appendCount"
            class="tab-badge"
          />
        </template>
        <div class="toolbar">
          <el-button
            type="danger"
            :disabled="selectedAppends.length === 0"
            @click="handleBatchDeleteAppends"
          >
            批量删除
            <span v-if="selectedAppends.length > 0"
              >({{ selectedAppends.length }})</span
            >
          </el-button>
        </div>
        <el-table
          :data="appendList"
          v-loading="appendLoading"
          border
          @selection-change="(val) => (selectedAppends = val)"
        >
          <el-table-column type="selection" width="45" />
          <el-table-column prop="userName" label="用户" width="120" />
          <el-table-column
            prop="goodsName"
            label="商品"
            min-width="150"
            show-overflow-tooltip
          />
          <el-table-column prop="appendNum" label="第几次" width="80" />
          <el-table-column
            prop="appendContent"
            label="追评内容"
            min-width="200"
            show-overflow-tooltip
          />
          <el-table-column label="图片" width="120">
            <template #default="{ row }">
              <div
                v-if="parseJson(row.appendImg).length > 0"
                class="media-preview"
              >
                <img
                  v-for="(img, idx) in parseJson(row.appendImg).slice(0, 2)"
                  :key="idx"
                  :src="img"
                  class="thumb-img"
                  @click="previewImage(img)"
                />
                <span
                  v-if="parseJson(row.appendImg).length > 2"
                  class="more-count"
                  >+{{ parseJson(row.appendImg).length - 2 }}</span
                >
              </div>
              <span v-else class="no-media">-</span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.status === 0" type="warning" size="small"
                >待审核</el-tag
              >
              <el-tag v-else-if="row.status === 1" type="success" size="small"
                >已通过</el-tag
              >
              <el-tag v-else type="danger" size="small">已拒绝</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="appendTime" label="时间" width="180" />
          <el-table-column label="操作" width="220" fixed="right">
            <template #default="{ row }">
              <template v-if="row.status === 0">
                <el-button-group>
                  <el-button
                    type="success"
                    size="small"
                    @click="handleApproveAppend(row)"
                    >通过</el-button
                  >
                  <el-button
                    type="warning"
                    size="small"
                    plain
                    @click="handleRejectAppend(row)"
                    >拒绝</el-button
                  >
                </el-button-group>
                <el-button
                  type="danger"
                  size="small"
                  text
                  @click="handleDeleteAppend(row.id)"
                  style="margin-left: 4px"
                  >删除</el-button
                >
              </template>
              <template v-else>
                <span style="color: #999; font-size: 12px; margin-right: 8px"
                  >已处理</span
                >
                <el-button
                  type="danger"
                  size="small"
                  text
                  @click="handleDeleteAppend(row.id)"
                  >删除</el-button
                >
              </template>
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination">
          <el-pagination
            v-model:current-page="appendPage"
            :page-size="10"
            :total="appendTotal"
            layout="total, prev, pager, next"
            @current-change="loadAppends"
          />
        </div>
      </el-tab-pane>

      <!-- 评价评论 -->
      <el-tab-pane name="reviewComment">
        <template #label>
          评价评论
          <el-badge
            v-if="pendingCounts.reviewCommentCount > 0"
            :value="pendingCounts.reviewCommentCount"
            class="tab-badge"
          />
        </template>
        <div class="toolbar">
          <el-button
            type="danger"
            :disabled="selectedReviewComments.length === 0"
            @click="handleBatchDeleteReviewComments"
          >
            批量删除
            <span v-if="selectedReviewComments.length > 0"
              >({{ selectedReviewComments.length }})</span
            >
          </el-button>
        </div>
        <el-table
          :data="reviewCommentList"
          v-loading="reviewCommentLoading"
          border
          @selection-change="(val) => (selectedReviewComments = val)"
        >
          <el-table-column type="selection" width="45" />
          <el-table-column prop="userName" label="用户" width="120" />
          <el-table-column
            prop="content"
            label="评论内容"
            min-width="200"
            show-overflow-tooltip
          />
          <el-table-column prop="replyToUserName" label="回复谁" width="120">
            <template #default="{ row }">
              <span>{{ row.replyToUserName || "-" }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.status === 0" type="warning" size="small"
                >待审核</el-tag
              >
              <el-tag v-else-if="row.status === 1" type="success" size="small"
                >已通过</el-tag
              >
              <el-tag v-else type="danger" size="small">已拒绝</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="时间" width="180" />
          <el-table-column label="操作" width="220" fixed="right">
            <template #default="{ row }">
              <template v-if="row.status === 0">
                <el-button-group>
                  <el-button
                    type="success"
                    size="small"
                    @click="handleApproveReviewComment(row)"
                    >通过</el-button
                  >
                  <el-button
                    type="warning"
                    size="small"
                    plain
                    @click="handleRejectReviewComment(row)"
                    >拒绝</el-button
                  >
                </el-button-group>
                <el-button
                  type="danger"
                  size="small"
                  text
                  @click="handleDeleteReviewComment(row.id)"
                  style="margin-left: 4px"
                  >删除</el-button
                >
              </template>
              <template v-else>
                <span style="color: #999; font-size: 12px; margin-right: 8px"
                  >已处理</span
                >
                <el-button
                  type="danger"
                  size="small"
                  text
                  @click="handleDeleteReviewComment(row.id)"
                  >删除</el-button
                >
              </template>
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination">
          <el-pagination
            v-model:current-page="reviewCommentPage"
            :page-size="10"
            :total="reviewCommentTotal"
            layout="total, prev, pager, next"
            @current-change="loadReviewComments"
          />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 图片预览 -->
    <el-dialog v-model="imagePreviewVisible" title="图片预览" width="600px">
      <img :src="previewImageUrl" style="width: 100%; border-radius: 6px" />
    </el-dialog>

    <!-- 视频预览 -->
    <el-dialog v-model="videoPreviewVisible" title="视频预览" width="600px">
      <video
        :src="previewVideoUrl"
        controls
        style="width: 100%; border-radius: 6px"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  approveAppend,
  approveComment,
  approveReviewComment,
  batchDeleteAppends,
  batchDeleteComments,
  batchDeleteReviewComments,
  deleteAppend,
  deleteComment,
  deleteReviewComment,
  getPendingAppends,
  getPendingCommentCount,
  getPendingComments,
  getPendingReviewComments,
  rejectAppend,
  rejectComment,
  rejectReviewComment,
} from "@/api/admin/comment.js";
import { logger } from "@/utils/logger.js";

const activeTab = ref("comment");

const commentList = ref([]);
const commentLoading = ref(false);
const commentPage = ref(1);
const commentTotal = ref(0);

const appendList = ref([]);
const appendLoading = ref(false);
const appendPage = ref(1);
const appendTotal = ref(0);

const reviewCommentList = ref([]);
const reviewCommentLoading = ref(false);
const reviewCommentPage = ref(1);
const reviewCommentTotal = ref(0);

const selectedComments = ref([]);
const selectedAppends = ref([]);
const selectedReviewComments = ref([]);

const pendingCounts = ref({
  commentCount: 0,
  appendCount: 0,
  reviewCommentCount: 0,
});
const totalPending = computed(
  () =>
    pendingCounts.value.commentCount +
    pendingCounts.value.appendCount +
    pendingCounts.value.reviewCommentCount,
);

// 图片视频预览
const imagePreviewVisible = ref(false);
const previewImageUrl = ref("");
const videoPreviewVisible = ref(false);
const previewVideoUrl = ref("");

const previewImage = (url) => {
  previewImageUrl.value = url;
  imagePreviewVisible.value = true;
};

const previewVideo = (url) => {
  previewVideoUrl.value = url;
  videoPreviewVisible.value = true;
};

const parseJson = (str) => {
  if (!str) return [];
  try {
    const arr = JSON.parse(str);
    return Array.isArray(arr) ? arr : [];
  } catch {
    return [];
  }
};

const loadComments = async () => {
  commentLoading.value = true;
  try {
    const res = await getPendingComments({
      current: commentPage.value,
      size: 10,
    });
    if (res.success || res.code === 200) {
      commentList.value = res.data.records || [];
      commentTotal.value = res.data.total || 0;
    }
  } catch (e) {
    logger.error(e);
  } finally {
    commentLoading.value = false;
  }
  fetchPendingCounts();
};

const loadAppends = async () => {
  appendLoading.value = true;
  try {
    const res = await getPendingAppends({
      current: appendPage.value,
      size: 10,
    });
    if (res.success || res.code === 200) {
      appendList.value = res.data.records || [];
      appendTotal.value = res.data.total || 0;
    }
  } catch (e) {
    logger.error(e);
  } finally {
    appendLoading.value = false;
  }
  fetchPendingCounts();
};

const loadReviewComments = async () => {
  reviewCommentLoading.value = true;
  try {
    const res = await getPendingReviewComments({
      current: reviewCommentPage.value,
      size: 10,
    });
    if (res.success || res.code === 200) {
      reviewCommentList.value = res.data.records || [];
      reviewCommentTotal.value = res.data.total || 0;
    }
  } catch (e) {
    logger.error(e);
  } finally {
    reviewCommentLoading.value = false;
  }
  fetchPendingCounts();
};

const handleTabChange = () => {
  if (activeTab.value === "comment") loadComments();
  else if (activeTab.value === "append") loadAppends();
  else if (activeTab.value === "reviewComment") loadReviewComments();
};

const handleApproveComment = async (row) => {
  try {
    await ElMessageBox.confirm("确定通过该评价吗？", "审核确认", {
      type: "success",
    });
    const res = await approveComment(row.id);
    if (res.success || res.code === 200) {
      ElMessage.success("审核通过");
      loadComments();
    }
  } catch (e) {
    if (e !== "cancel") ElMessage.error("操作失败");
  }
};

const handleRejectComment = async (row) => {
  try {
    await ElMessageBox.confirm("确定拒绝该评价吗？", "审核确认", {
      type: "warning",
    });
    const res = await rejectComment(row.id);
    if (res.success || res.code === 200) {
      ElMessage.success("已拒绝");
      loadComments();
    }
  } catch (e) {
    if (e !== "cancel") ElMessage.error("操作失败");
  }
};

const handleApproveAppend = async (row) => {
  try {
    await ElMessageBox.confirm("确定通过该追评吗？", "审核确认", {
      type: "success",
    });
    const res = await approveAppend(row.id);
    if (res.success || res.code === 200) {
      ElMessage.success("审核通过");
      loadAppends();
    }
  } catch (e) {
    if (e !== "cancel") ElMessage.error("操作失败");
  }
};

const handleRejectAppend = async (row) => {
  try {
    await ElMessageBox.confirm("确定拒绝该追评吗？", "审核确认", {
      type: "warning",
    });
    const res = await rejectAppend(row.id);
    if (res.success || res.code === 200) {
      ElMessage.success("已拒绝");
      loadAppends();
    }
  } catch (e) {
    if (e !== "cancel") ElMessage.error("操作失败");
  }
};

const handleApproveReviewComment = async (row) => {
  try {
    await ElMessageBox.confirm("确定通过该评论吗？", "审核确认", {
      type: "success",
    });
    const res = await approveReviewComment(row.id);
    if (res.success || res.code === 200) {
      ElMessage.success("审核通过");
      loadReviewComments();
    }
  } catch (e) {
    if (e !== "cancel") ElMessage.error("操作失败");
  }
};

const handleRejectReviewComment = async (row) => {
  try {
    await ElMessageBox.confirm("确定拒绝该评论吗？", "审核确认", {
      type: "warning",
    });
    const res = await rejectReviewComment(row.id);
    if (res.success || res.code === 200) {
      ElMessage.success("已拒绝");
      loadReviewComments();
    }
  } catch (e) {
    if (e !== "cancel") ElMessage.error("操作失败");
  }
};

// ========== 删除处理 ==========

const handleDeleteComment = async (id) => {
  try {
    await ElMessageBox.confirm("确定删除该评价吗？", "确认删除", {
      type: "warning",
    });
    const res = await deleteComment(id);
    if (res.success || res.code === 200) {
      ElMessage.success("删除成功");
      loadComments();
    }
  } catch (e) {
    if (e !== "cancel") ElMessage.error("操作失败");
  }
};

const handleBatchDeleteComments = async () => {
  const ids = selectedComments.value.map((r) => r.id);
  if (ids.length === 0) return;
  try {
    await ElMessageBox.confirm(
      `确定删除选中的 ${ids.length} 条评价吗？`,
      "批量删除",
      {
        confirmButtonText: "确定删除",
        cancelButtonText: "取消",
        type: "warning",
      },
    );
    const res = await batchDeleteComments(ids);
    if (res.success || res.code === 200) {
      ElMessage.success(`已删除 ${ids.length} 条`);
      selectedComments.value = [];
      loadComments();
    }
  } catch (e) {
    if (e !== "cancel") ElMessage.error("操作失败");
  }
};

const handleDeleteAppend = async (id) => {
  try {
    await ElMessageBox.confirm("确定删除该追评吗？", "确认删除", {
      type: "warning",
    });
    const res = await deleteAppend(id);
    if (res.success || res.code === 200) {
      ElMessage.success("删除成功");
      loadAppends();
    }
  } catch (e) {
    if (e !== "cancel") ElMessage.error("操作失败");
  }
};

const handleBatchDeleteAppends = async () => {
  const ids = selectedAppends.value.map((r) => r.id);
  if (ids.length === 0) return;
  try {
    await ElMessageBox.confirm(
      `确定删除选中的 ${ids.length} 条追评吗？`,
      "批量删除",
      {
        confirmButtonText: "确定删除",
        cancelButtonText: "取消",
        type: "warning",
      },
    );
    const res = await batchDeleteAppends(ids);
    if (res.success || res.code === 200) {
      ElMessage.success(`已删除 ${ids.length} 条`);
      selectedAppends.value = [];
      loadAppends();
    }
  } catch (e) {
    if (e !== "cancel") ElMessage.error("操作失败");
  }
};

const handleDeleteReviewComment = async (id) => {
  try {
    await ElMessageBox.confirm("确定删除该评论吗？", "确认删除", {
      type: "warning",
    });
    const res = await deleteReviewComment(id);
    if (res.success || res.code === 200) {
      ElMessage.success("删除成功");
      loadReviewComments();
    }
  } catch (e) {
    if (e !== "cancel") ElMessage.error("操作失败");
  }
};

const handleBatchDeleteReviewComments = async () => {
  const ids = selectedReviewComments.value.map((r) => r.id);
  if (ids.length === 0) return;
  try {
    await ElMessageBox.confirm(
      `确定删除选中的 ${ids.length} 条评论吗？`,
      "批量删除",
      {
        confirmButtonText: "确定删除",
        cancelButtonText: "取消",
        type: "warning",
      },
    );
    const res = await batchDeleteReviewComments(ids);
    if (res.success || res.code === 200) {
      ElMessage.success(`已删除 ${ids.length} 条`);
      selectedReviewComments.value = [];
      loadReviewComments();
    }
  } catch (e) {
    if (e !== "cancel") ElMessage.error("操作失败");
  }
};

const fetchPendingCounts = async () => {
  try {
    const res = await getPendingCommentCount();
    if (res.success || res.code === 200) {
      pendingCounts.value = res.data || {
        commentCount: 0,
        appendCount: 0,
        reviewCommentCount: 0,
      };
    }
  } catch (e) {
    /* ignore */
  }
};

onMounted(() => {
  loadComments();
  fetchPendingCounts();
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

.toolbar {
  margin-bottom: 12px;
  display: flex;
  gap: 10px;
}

.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

.media-preview {
  display: flex;
  align-items: center;
  gap: 4px;
}

.thumb-img {
  width: 40px;
  height: 40px;
  border-radius: 4px;
  object-fit: cover;
  cursor: pointer;
  border: 1px solid #eee;
}

.thumb-img:hover {
  opacity: 0.8;
}

.more-count {
  font-size: 12px;
  color: #999;
}

.pending-alert {
  margin-bottom: 16px;
}

.tab-badge {
  margin-left: 6px;
}

.tab-badge :deep(.el-badge__content) {
  font-size: 11px;
  height: 18px;
  line-height: 18px;
  padding: 0 6px;
}

.no-media {
  color: #ccc;
}
</style>
