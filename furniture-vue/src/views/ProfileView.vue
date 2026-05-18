<template>
    <div class="profile-page">
        <!-- 顶部导航栏 -->
        <div class="top-nav">
            <div class="nav-content">
                <el-button text @click="goHome" class="back-btn">
                    <el-icon>
                        <ArrowLeft />
                    </el-icon>
                    返回首页
                </el-button>
                <div class="breadcrumb">首页 / 个人中心</div>
            </div>
        </div>

        <div class="profile-container">
            <!-- 左侧：用户形象与核心状态 -->
            <div class="user-hero-card">
                <div class="hero-bg"></div>
                <div class="hero-content">
                    <div class="avatar-wrapper">
                        <img class="large-avatar"
                            :src="userInfo.icon ? 'http://localhost:8080' + userInfo.icon : '/images/default-avatar.png'" />
                        <div class="avatar-badge" :class="{ 'active': userInfo.hasPassword }">
                            {{ userInfo.hasPassword ? '🛡️' : '⚠️' }}
                        </div>
                    </div>
                    <div class="user-welcome">
                        <h1 class="user-name">{{ userInfo.userName || '未知用户' }}</h1>
                        <div class="security-tag">
                            <el-tag :type="userInfo.hasPassword ? 'success' : 'warning'" size="small" effect="dark">
                                {{ userInfo.hasPassword ? '账户安全等级：高' : '建议设置密码' }}
                            </el-tag>
                        </div>

                        <!-- 收货信息概览 -->
                        <div class="delivery-info-preview" v-if="userInfo.consignee || userInfo.address">
                            <div class="info-row">
                                <el-icon>
                                    <User />
                                </el-icon>
                                <span class="label">收货人:</span>
                                <span class="value">{{ userInfo.consignee }}</span>
                            </div>
                            <div class="info-row" v-if="userInfo.consigneePhone">
                                <el-icon>
                                    <Phone />
                                </el-icon>
                                <span class="label">电话:</span>
                                <span class="value">{{ userInfo.consigneePhone }}</span>
                            </div>
                            <div class="info-row" v-if="userInfo.address">
                                <el-icon>
                                    <Location />
                                </el-icon>
                                <span class="label">地址:</span>
                                <span class="value address-text">{{ userInfo.address }}</span>
                            </div>
                        </div>
                        <div v-else class="delivery-empty-tip">
                            <el-text type="info" size="small">暂无收货信息，请点击编辑完善</el-text>
                        </div>
                    </div>

                    <el-button type="primary" plain @click="openEditDialog" class="edit-profile-btn">
                        <el-icon>
                            <Edit />
                        </el-icon> 编辑资料
                    </el-button>
                </div>
            </div>

            <!-- 右侧：功能网格 -->
            <div class="features-grid">
                <!-- 卡片 1: 账号安全 -->
                <el-card shadow="hover" class="feature-card security-card" @click="openPasswordDialog">
                    <div class="card-icon-box" :style="{ background: userInfo.hasPassword ? '#e1f3d8' : '#feefea' }">
                        <el-icon :size="28" :color="userInfo.hasPassword ? '#67c23a' : '#f56c6c'">
                            <Lock />
                        </el-icon>
                    </div>
                    <div class="card-info">
                        <h3>{{ userInfo.hasPassword ? '修改密码' : '设置密码' }}</h3>
                        <p>{{ userInfo.hasPassword ? '定期更换密码保障账户安全' : '当前未设置密码，存在安全风险' }}</p>
                    </div>
                    <div class="card-arrow"><el-icon>
                            <ArrowRight />
                        </el-icon></div>
                </el-card>

                <!-- 卡片 2: 基本信息 -->
                <el-card shadow="hover" class="feature-card" @click="openEditDialog">
                    <div class="card-icon-box" style="background: #ecf5ff;">
                        <el-icon :size="28" color="#409eff">
                            <User />
                        </el-icon>
                    </div>
                    <div class="card-info">
                        <h3>基本信息</h3>
                        <p>管理昵称、收货地址及联系方式</p>
                    </div>
                    <div class="card-arrow"><el-icon>
                            <ArrowRight />
                        </el-icon></div>
                </el-card>

                <!-- 卡片 3: 购买记录 -->
                <el-card shadow="hover" class="feature-card" @click="console.log('clicked'); goToOrders()">
                    <div class="card-icon-box" style="background: #fdf6ec;">
                        <el-icon :size="28" color="#e6a23c">
                            <ShoppingCart />
                        </el-icon>
                    </div>
                    <div class="card-info">
                        <h3>购买记录</h3>
                        <p>查看历史订单及订单详情</p>
                    </div>
                    <div class="card-arrow"><el-icon>
                            <ArrowRight />
                        </el-icon></div>
                </el-card>

                <!-- 卡片 4: 退出登录 -->
                <el-card shadow="hover" class="feature-card logout-card" @click="handleLogout">
                    <div class="card-icon-box" style="background: #fef0f0;">
                        <el-icon :size="28" color="#f56c6c">
                            <SwitchButton />
                        </el-icon>
                    </div>
                    <div class="card-info">
                        <h3>退出登录</h3>
                        <p>安全退出当前账户</p>
                    </div>
                    <div class="card-arrow"><el-icon>
                            <ArrowRight />
                        </el-icon></div>
                </el-card>
            </div>
        </div>

        <!-- 弹窗：修改基本信息 -->
        <el-dialog v-model="editDialogVisible" title="编辑个人资料" width="500px" :close-on-click-modal="false">
            <div class="dialog-header-tip">完善您的个人信息及收货地址，以便更好地为您服务。</div>

            <el-form :model="editForm" label-width="90px" style="margin-top: 20px;" ref="editFormRef">
                <el-form-item label="昵称" prop="userName"
                    :rules="[{ required: true, message: '请输入昵称', trigger: 'blur' }]">
                    <el-input v-model="editForm.userName" placeholder="请输入您的昵称" maxlength="20" show-word-limit />
                </el-form-item>

                <el-form-item label="头像">
                    <div class="avatar-upload-wrapper">
                        <img class="preview-img"
                            :src="editForm.icon ? 'http://localhost:8080' + editForm.icon : '/images/default-avatar.png'" />
                        <div class="upload-action">
                            <input type="file" accept="image/*" ref="fileInput" @change="onFileChange"
                                style="display: none" />
                            <el-button size="small" @click="fileInput.click()" :loading="uploading">
                                更换头像
                            </el-button>
                            <span v-if="uploading" class="upload-tip">上传中...</span>
                        </div>
                    </div>
                </el-form-item>

                <el-divider content-position="left">收货信息</el-divider>

                <el-form-item label="收货人" prop="consignee"
                    :rules="[{ required: true, message: '请输入收货人姓名', trigger: 'blur' }]">
                    <el-input v-model="editForm.consignee" placeholder="例如：张三" maxlength="20" />
                </el-form-item>

                <el-form-item label="收货电话" prop="consigneePhone" :rules="[
                    { required: true, message: '请输入收货电话', trigger: 'blur' },
                    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
                ]">
                    <el-input v-model="editForm.consigneePhone" placeholder="默认使用登录手机号" maxlength="11" />
                    <div class="form-item-tip">默认填充您的登录账号，如有不同可修改</div>
                </el-form-item>

                <el-form-item label="收货地址" prop="address"
                    :rules="[{ required: true, message: '请输入详细收货地址', trigger: 'blur' }]">
                    <el-input v-model="editForm.address" type="textarea" :rows="3" placeholder="省/市/区 + 街道门牌号"
                        maxlength="100" show-word-limit />
                </el-form-item>
            </el-form>

            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="editDialogVisible = false">取消</el-button>
                    <el-button type="primary" @click="submitEdit(editFormRef)" :loading="submitting">保存修改</el-button>
                </span>
            </template>
        </el-dialog>

        <!-- 弹窗：设置/修改密码 -->
        <el-dialog v-model="pwdDialogVisible" :title="userInfo.hasPassword ? '修改密码' : '设置密码'" width="450px"
            :close-on-click-modal="false">
            <div class="dialog-header-tip" :class="!userInfo.hasPassword ? 'warning-tip' : ''">
                {{ userInfo.hasPassword ? '为了账户安全，建议每 3 个月更换一次密码。' : '为了保护账户安全，请务必设置登录密码。' }}
            </div>
            <el-form :model="pwdForm" label-width="90px" :rules="pwdRules" ref="pwdFormRef" style="margin-top: 20px;">
                <el-form-item v-if="userInfo.hasPassword" label="旧密码" prop="oldPassword">
                    <el-input v-model="pwdForm.oldPassword" type="password" placeholder="请输入当前密码" show-password />
                </el-form-item>
                <el-form-item label="新密码" prop="newPassword">
                    <el-input v-model="pwdForm.newPassword" type="password" placeholder="6-20位字母或数字" show-password />
                </el-form-item>
                <el-form-item label="确认密码" prop="confirmPassword">
                    <el-input v-model="pwdForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
                </el-form-item>
            </el-form>
            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="pwdDialogVisible = false">取消</el-button>
                    <el-button type="primary" @click="submitPassword(pwdFormRef)" :loading="submitting">确认提交</el-button>
                </span>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, Edit, Lock, User, ShoppingCart, Phone, ArrowRight, SwitchButton, Location } from '@element-plus/icons-vue'
import { useProfile } from '@/composables/useProfile.js'
import { uploadAvatar } from '@/api/user.js'
import { ElMessage } from 'element-plus'
import '@/styles/views/profile.scss'

const editFormRef = ref(null)
const pwdFormRef = ref(null)
const fileInput = ref(null)
const router = useRouter()

const {
    userInfo,
    editDialogVisible,
    pwdDialogVisible,
    submitting,
    uploading,
    editForm,
    pwdForm,
    pwdRules,
    loadUserInfo,
    handleLogout,
    openEditDialog,
    submitEdit,
    openPasswordDialog,
    handleAvatarUpload,
    submitPassword,
    handleImageError,
    goHome
} = useProfile()


const goToOrders = () => {
    console.log('点击了购买记录，准备跳转')
    router.push('/user/orders')
}

const onFileChange = async (e) => {
    const file = e.target.files[0]
    if (!file) return

    uploading.value = true
    try {
        const res = await uploadAvatar(file)
        if (res.success) {
            editForm.icon = res.data
            ElMessage.success('头像上传成功，点击保存生效')
        } else {
            ElMessage.error(res.message || '上传失败')
        }
    } catch (error) {
        console.error('上传错误:', error)
        ElMessage.error('上传出错')
    } finally {
        uploading.value = false
    }
    e.target.value = ''
}



onMounted(() => {
    loadUserInfo()
})
</script>