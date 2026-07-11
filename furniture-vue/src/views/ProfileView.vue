<template>
  <div class="profile-page">
    <!-- Breadcrumb -->
    <div class="page-breadcrumb">
      <router-link to="/">首页</router-link>
      <span>/</span>
      <span class="current">个人中心</span>
    </div>

    <div class="profile-container">
      <!-- 左侧：用户卡片 -->
      <div class="user-hero-card">
        <div class="hero-bg"></div>
        <div class="hero-content">
          <div class="avatar-wrapper">
            <img class="large-avatar"
                 :src="imgUrl(userInfo.icon, '/images/default-avatar.png')"
                 @error="e => e.target.src = '/images/default-avatar.png'"/>
            <div class="avatar-badge" :class="{ active: userInfo.hasPassword }">
              <el-icon v-if="userInfo.hasPassword" :size="14" color="#5b8c5a"><Lock/></el-icon>
              <el-icon v-else :size="14" color="#c8843a"><Warning/></el-icon>
            </div>
          </div>
          <div class="user-welcome">
            <h1 class="user-name">{{ userInfo.userName || '未知用户' }}</h1>
            <div class="meta-row">
              <el-icon><Phone/></el-icon>
              <span>{{ userInfo.phone || '未绑定手机' }}</span>
            </div>
            <div class="meta-row">
              <el-icon><Message/></el-icon>
              <span>{{ userInfo.email || '未绑定邮箱' }}</span>
            </div>
            <div class="meta-row" v-if="userInfo.createTime">
              <el-icon><Clock/></el-icon>
              <span>注册时间：{{ formatTime(userInfo.createTime) }}</span>
            </div>
            <div class="security-tag">
              <el-tag :type="userInfo.hasPassword ? 'success' : 'warning'" size="small" effect="dark">
                {{ userInfo.hasPassword ? '已设置密码' : '建议设置密码' }}
              </el-tag>
            </div>
          </div>
          <el-button type="primary" plain @click="openEditDialog" class="edit-btn">编辑资料</el-button>
        </div>
      </div>

      <!-- 右侧：功能网格 -->
      <div class="features-grid">
        <el-card shadow="hover" class="feature-card" @click="openPasswordDialog">
          <div class="card-icon-box" :style="{ background: userInfo.hasPassword ? '#f0f2f0' : '#faf8f5' }">
            <el-icon :size="28" :color="userInfo.hasPassword ? '#2c2c2c' : '#999'"><Lock/></el-icon>
          </div>
          <div class="card-info">
            <h3>{{ userInfo.hasPassword ? '修改密码' : '设置密码' }}</h3>
            <p>{{ userInfo.hasPassword ? '定期更换密码保障账户安全' : '当前未设置密码，存在安全风险' }}</p>
          </div>
          <div class="card-arrow"><el-icon><ArrowRight/></el-icon></div>
        </el-card>

        <el-card shadow="hover" class="feature-card" @click="openEditDialog">
          <div class="card-icon-box" style="background: #f0f2f0;"><el-icon :size="28" color="#2c2c2c"><User/></el-icon></div>
          <div class="card-info"><h3>基本信息</h3><p>管理昵称、收货地址及联系方式</p></div>
          <div class="card-arrow"><el-icon><ArrowRight/></el-icon></div>
        </el-card>

        <el-card shadow="hover" class="feature-card" @click="goToOrders">
          <div class="card-icon-box" style="background: #f0f2f0;"><el-icon :size="28" color="#2c2c2c"><ShoppingCart/></el-icon></div>
          <div class="card-info"><h3>购买记录</h3><p>查看历史订单及订单详情</p></div>
          <div class="card-arrow"><el-icon><ArrowRight/></el-icon></div>
        </el-card>

        <el-card shadow="hover" class="feature-card" @click="goToFavorites">
          <div class="card-icon-box" style="background: #faf3eb;"><el-icon :size="28" color="#b8753e"><Star/></el-icon></div>
          <div class="card-info"><h3>我的收藏</h3><p>查看已收藏的心仪家具</p></div>
          <div class="card-arrow"><el-icon><ArrowRight/></el-icon></div>
        </el-card>

        <el-card shadow="hover" class="feature-card" @click="goToAddresses">
          <div class="card-icon-box" style="background: #eef2f7;"><el-icon :size="28" color="#5a7fa0"><Location/></el-icon></div>
          <div class="card-info"><h3>收货地址</h3><p>管理常用收货地址</p></div>
          <div class="card-arrow"><el-icon><ArrowRight/></el-icon></div>
        </el-card>

        <el-card shadow="hover" class="feature-card" @click="goToNotifications">
          <div class="card-icon-box" style="background: #f0f2f0;"><el-icon :size="28" color="#2c2c2c"><Bell/></el-icon></div>
          <div class="card-info"><h3>消息通知</h3><p>查看系统消息与通知</p></div>
          <div class="card-arrow"><el-icon><ArrowRight/></el-icon></div>
        </el-card>

        <el-card shadow="hover" class="feature-card logout-card" @click="handleLogout">
          <div class="card-icon-box" style="background: #f5f5f5;"><el-icon :size="28" color="#999"><SwitchButton/></el-icon></div>
          <div class="card-info"><h3>退出登录</h3><p>安全退出当前账户</p></div>
          <div class="card-arrow"><el-icon><ArrowRight/></el-icon></div>
        </el-card>
      </div>
    </div>

    <!-- 编辑资料弹窗 -->
    <el-dialog v-model="editDialogVisible" title="编辑个人资料" width="500px" :close-on-click-modal="false">
      <div class="dialog-header-tip">完善您的个人信息，收货地址请在"收货地址"中管理。</div>
      <el-form :model="editForm" label-width="90px" style="margin-top: 20px;" ref="editFormRef">
        <el-form-item label="昵称" prop="userName" :rules="[{ required: true, message: '请输入昵称', trigger: 'blur' }]">
          <el-input v-model="editForm.userName" placeholder="请输入您的昵称" maxlength="20" show-word-limit/>
        </el-form-item>
        <el-form-item label="邮箱" prop="email" :rules="[{ required: true, message: '请输入邮箱', trigger: 'blur' }, { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }]">
          <el-input v-model="editForm.email" placeholder="绑定/更换邮箱地址" maxlength="100"/>
        </el-form-item>
        <el-form-item label="头像">
          <div class="avatar-upload-wrapper">
            <img class="preview-img" :src="imgUrl(editForm.icon, '/images/default-avatar.png')"/>
            <div class="upload-action">
              <input type="file" accept="image/*" ref="fileInput" @change="onFileChange" style="display: none"/>
              <el-button size="small" @click="fileInput.click()" :loading="uploading">更换头像</el-button>
              <span v-if="uploading" class="upload-tip">上传中...</span>
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit(editFormRef)" :loading="submitting">保存修改</el-button>
      </template>
    </el-dialog>

    <!-- 修改密码弹窗 -->
    <el-dialog v-model="pwdDialogVisible" :title="userInfo.hasPassword ? '修改密码' : '设置密码'" width="450px" :close-on-click-modal="false">
      <div class="dialog-header-tip" :class="!userInfo.hasPassword ? 'warning-tip' : ''">
        {{ userInfo.hasPassword ? '为了账户安全，建议每 3 个月更换一次密码。' : '为了保护账户安全，请务必设置登录密码。' }}
      </div>
      <el-form :model="pwdForm" label-width="90px" :rules="pwdRules" ref="pwdFormRef" style="margin-top: 20px;">
        <el-form-item v-if="userInfo.hasPassword" label="旧密码" prop="oldPassword">
          <el-input v-model="pwdForm.oldPassword" type="password" placeholder="请输入当前密码" show-password/>
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" placeholder="6-20位字母或数字" show-password/>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="pwdForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPassword(pwdFormRef)" :loading="submitting">确认提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {onMounted, ref} from 'vue'
import {useRouter} from 'vue-router'
import {ArrowRight, Bell, Clock, Edit, Location, Lock, Message, Phone, ShoppingCart, Star, SwitchButton, User, Warning} from '@element-plus/icons-vue'
import {useProfile} from '@/composables/useProfile.js'
import {imgUrl} from '@/utils/img.js'
import {formatTime} from '@/utils/format.js'
import {logger} from '@/utils/logger.js'
import {uploadAvatar} from '@/api/user.js'
import {ElMessage} from 'element-plus'

const editFormRef = ref(null)
const pwdFormRef = ref(null)
const fileInput = ref(null)
const router = useRouter()

const {userInfo, editDialogVisible, pwdDialogVisible, submitting, uploading, editForm, pwdForm, pwdRules, loadUserInfo, handleLogout, openEditDialog, submitEdit, openPasswordDialog, submitPassword, goHome} = useProfile()

const goToOrders = () => router.push('/user/orders')
const goToFavorites = () => router.push('/user/favorites')
const goToNotifications = () => router.push('/notification')
const goToAddresses = () => router.push('/user/addresses')

const onFileChange = async (e) => {
  const file = e.target.files[0]
  if (!file) return
  uploading.value = true
  try {
    const res = await uploadAvatar(file)
    if (res.success) { editForm.icon = res.data; ElMessage.success('头像上传成功，点击保存生效') }
    else ElMessage.error(res.msg || '上传失败')
  } catch (error) { logger.error('上传错误:', error) }
  finally { uploading.value = false }
  e.target.value = ''
}

onMounted(() => { loadUserInfo() })
</script>

<style scoped>
/* Breadcrumb */
.page-breadcrumb {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: var(--space-4) var(--space-6);
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--text-xs);
  color: var(--color-text-tertiary);
}
.page-breadcrumb a { color: var(--color-text-tertiary); text-decoration: none; }
.page-breadcrumb a:hover { color: var(--color-text-primary); }
.current { color: var(--color-text-primary); }

/* Layout */
.profile-container {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 0 var(--space-6) var(--space-12);
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: var(--space-6);
}

/* Hero card */
.user-hero-card {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  overflow: hidden;
  border: 1px solid var(--color-border-light);
  text-align: center;
  height: fit-content;
}
.hero-bg { height: 80px; background: #f5f0ea; }
.hero-content { padding: 0 var(--space-5) var(--space-6); margin-top: -44px; }
.avatar-wrapper { position: relative; display: inline-block; margin-bottom: var(--space-3); }
.large-avatar {
  width: 88px; height: 88px;
  border-radius: 50%;
  border: 4px solid #fff;
  box-shadow: var(--shadow-sm);
  object-fit: cover;
}
.avatar-badge {
  position: absolute; bottom: 4px; right: 4px;
  background: #fff; border-radius: 50%; padding: 3px;
  box-shadow: var(--shadow-xs);
}
.user-name { font-size: var(--text-lg); font-weight: 700; color: var(--color-text-primary); margin-bottom: var(--space-3); }
.meta-row {
  display: flex; align-items: center; justify-content: center; gap: var(--space-1);
  font-size: var(--text-xs); color: var(--color-text-tertiary); margin-bottom: var(--space-1);
}
.security-tag { margin: var(--space-3) 0; }
.edit-btn { width: 100%; border-radius: var(--radius-full); margin-top: var(--space-2); }

/* Feature cards */
.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: var(--space-4);
  align-content: start;
}
.feature-card {
  cursor: pointer;
  border: 1px solid var(--color-border-light) !important;
  border-radius: var(--radius-lg) !important;
  transition: all var(--transition-normal);
}
.feature-card:hover { transform: translateY(-2px); box-shadow: var(--shadow-md) !important; }
.feature-card :deep(.el-card__body) {
  display: flex; align-items: center; padding: var(--space-5); gap: var(--space-4);
}
.card-icon-box {
  width: 48px; height: 48px; border-radius: var(--radius-md);
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.card-info h3 { font-size: var(--text-sm); font-weight: 600; color: var(--color-text-primary); margin-bottom: 2px; }
.card-info p { font-size: var(--text-xs); color: var(--color-text-tertiary); margin: 0; }
.card-arrow { color: var(--color-border); margin-left: auto; transition: all var(--transition-fast); }
.feature-card:hover .card-arrow { color: var(--color-text-secondary); transform: translateX(2px); }
.logout-card h3 { color: var(--color-danger); }

/* Dialogs */
.dialog-header-tip {
  background: #f5f5f5; color: var(--color-text-tertiary);
  padding: var(--space-3) var(--space-4); border-radius: var(--radius-md);
  font-size: var(--text-xs);
}
.dialog-header-tip.warning-tip { background: #fef8f2; color: var(--color-warning); }
.avatar-upload-wrapper { display: flex; align-items: center; gap: var(--space-4); }
.preview-img { width: 56px; height: 56px; border-radius: 50%; object-fit: cover; border: 2px solid var(--color-border-light); }
.upload-action { display: flex; flex-direction: column; gap: var(--space-1); }
.upload-tip { font-size: var(--text-xs); color: var(--color-text-tertiary); }

@media (max-width: 768px) {
  .profile-container { grid-template-columns: 1fr; padding: 0 var(--space-4) var(--space-8); }
  .features-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>
