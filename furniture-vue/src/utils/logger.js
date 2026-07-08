/**
 * 条件日志工具：仅在开发环境输出，生产环境静默。
 * 替代裸 console.log / console.error，避免生产环境控制台信息泄露。
 */
const isDev = import.meta.env.DEV

export const logger = {
  log(...args) {
    if (isDev) console.log(...args)
  },
  error(...args) {
    if (isDev) console.error(...args)
  },
  warn(...args) {
    if (isDev) console.warn(...args)
  }
}
