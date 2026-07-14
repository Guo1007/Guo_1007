import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { getUserInfo, updatePassword, updateUserProfile } from "@/api/user";
import { useLogout } from "@/composables/useLogout.js";
import { logger } from "@/utils/logger.js";

export function useProfile() {
  const router = useRouter();
  const { logout } = useLogout();

  // 用户信息
  const userInfo = ref({
    userName: "",
    phone: "",
    email: "",
    icon: "",
    hasPassword: undefined,
    createTime: "",
  });

  // 弹窗控制
  const editDialogVisible = ref(false);
  const pwdDialogVisible = ref(false);
  const submitting = ref(false);
  const uploading = ref(false);

  // 编辑表单
  const editForm = reactive({
    userName: "",
    email: "",
    icon: "",
  });

  // 密码表单
  const pwdForm = reactive({
    oldPassword: "",
    newPassword: "",
    confirmPassword: "",
  });

  // 密码验证规则
  const validateConfirm = (rule, value, callback) => {
    if (value !== pwdForm.newPassword) {
      callback(new Error("两次输入的密码不一致"));
    } else {
      callback();
    }
  };

  const pwdRules = {
    oldPassword: [{ required: true, message: "请输入旧密码", trigger: "blur" }],
    newPassword: [
      { required: true, message: "请输入新密码", trigger: "blur" },
      { min: 6, message: "密码长度至少 6 位", trigger: "blur" },
      {
        pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d]{6,32}$/,
        message: "密码需6-32位，含大小写字母和数字",
        trigger: "blur",
      },
    ],
    confirmPassword: [
      { required: true, message: "请确认新密码", trigger: "blur" },
      { validator: validateConfirm, trigger: "blur" },
    ],
  };

  // 加载用户信息
  const loadUserInfo = async () => {
    try {
      const res = await getUserInfo();
      if (res.success && res.data) {
        userInfo.value = { ...userInfo.value, ...res.data };
        // 同步到 localStorage 供首页等页面使用
        syncUserToStorage(res.data);
      } else {
        ElMessage.error("获取用户信息失败");
        router.push("/login");
      }
    } catch (error) {
      logger.error("loadUserInfo:", error);
      router.push("/login");
    }
  };

  // 同步用户信息到 localStorage
  const syncUserToStorage = (data) => {
    localStorage.setItem("userInfo", JSON.stringify(data));
    if (data.userName) localStorage.setItem("userName", data.userName);
    if (data.icon) localStorage.setItem("userIcon", data.icon);
    if (data.email) localStorage.setItem("userEmail", data.email);
  };

  // 退出登录
  const handleLogout = logout;

  // 打开编辑对话框
  const openEditDialog = () => {
    editForm.userName = userInfo.value.userName || "";
    editForm.email = userInfo.value.email || "";
    editForm.icon = userInfo.value.icon || "";
    editDialogVisible.value = true;
  };

  // 提交编辑
  const submitEdit = async (formRef) => {
    if (!formRef) return;

    await formRef.validate(async (valid) => {
      if (!valid) return;

      submitting.value = true;
      try {
        const params = {
          userName: editForm.userName,
          email: editForm.email,
          icon: editForm.icon,
        };

        const res = await updateUserProfile(params);
        if (res.success) {
          ElMessage.success("修改成功");
          editDialogVisible.value = false;
          // 更新本地数据
          userInfo.value.userName = editForm.userName;
          userInfo.value.email = editForm.email;
          userInfo.value.icon = editForm.icon;
          // 同步到 localStorage
          const stored = JSON.parse(localStorage.getItem("userInfo") || "{}");
          stored.userName = editForm.userName;
          stored.icon = editForm.icon;
          syncUserToStorage(stored);
        } else {
          ElMessage.error(res.msg || "修改失败");
        }
      } catch (error) {
        logger.error("submitEdit:", error);
      } finally {
        submitting.value = false;
      }
    });
  };

  // 打开密码对话框
  const openPasswordDialog = () => {
    pwdForm.oldPassword = "";
    pwdForm.newPassword = "";
    pwdForm.confirmPassword = "";

    // 动态调整规则
    if (!userInfo.value.hasPassword) {
      pwdRules.oldPassword = [];
    } else {
      pwdRules.oldPassword = [
        { required: true, message: "请输入旧密码", trigger: "blur" },
      ];
    }
    pwdDialogVisible.value = true;
  };

  // 提交密码修改
  const submitPassword = async (formRef) => {
    if (!formRef) return;

    await formRef.validate(async (valid) => {
      if (!valid) return;

      submitting.value = true;
      try {
        const payload = {
          newPassword: pwdForm.newPassword,
          confirmPassword: pwdForm.confirmPassword,
          oldPassword: userInfo.value.hasPassword
            ? pwdForm.oldPassword
            : undefined,
        };
        const res = await updatePassword(payload);
        if (res.success) {
          ElMessage.success(
            userInfo.value.hasPassword ? "密码修改成功" : "密码设置成功",
          );
          pwdDialogVisible.value = false;
          userInfo.value.hasPassword = true;
        } else {
          ElMessage.error(res.msg || "操作失败");
        }
      } catch (error) {
        logger.error("submitPassword:", error);
      } finally {
        submitting.value = false;
      }
    });
  };

  const goHome = () => {
    router.push("/");
  };

  return {
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
    submitPassword,
    goHome,
  };
}
