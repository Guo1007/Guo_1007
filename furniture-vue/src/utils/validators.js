export const phoneRegex =
  /^1([38][0-9]|4[579]|5[0-35-9]|6[2567]|7[0-8]|9[0-9])\d{8}$/;

export const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

export const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d]{6,32}$/;

export const validatePhone = (phone) => {
  if (!phone) return "请输入手机号";
  if (!phoneRegex.test(phone)) return "手机号格式不正确";
  return "";
};

export const validateEmail = (email) => {
  if (!email) return "请输入邮箱";
  if (!emailRegex.test(email)) return "邮箱格式不正确";
  return "";
};

export const validatePassword = (password) => {
  if (!password) return "请输入密码";
  if (!passwordRegex.test(password))
    return "密码需6位以上，且必须包含大小写字母和数字";
  return "";
};

export const validateConfirmPassword = (password, confirmPassword) => {
  if (!confirmPassword) return "请确认密码";
  if (password !== confirmPassword) return "两次输入的密码不一致";
  return "";
};
