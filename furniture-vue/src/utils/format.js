/**
 * 格式化价格为两位小数
 */
export function formatPrice(price) {
  if (!price) return '0.00'
  return parseFloat(price).toFixed(2)
}

/**
 * 格式化时间字符串（替换 T 为空格，截取前19位 yyyy-MM-dd HH:mm:ss）
 */
export function formatTime(t) {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 19)
}

/**
 * 格式化为完整中文时间（含年月日时分）
 */
export function formatTimeFull(time) {
  if (!time) return '-'
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}
