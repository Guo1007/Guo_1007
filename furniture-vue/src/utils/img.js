const IMAGE_BASE = 'http://localhost:8080'

/**
 * 构造可用的图片 URL
 * @param {string} path  - 图片路径（可能是相对路径或完整 URL）
 * @param {string} fallback - 当 path 为空时的默认图
 * @returns {string} 完整的可访问图片 URL
 */
export function imgUrl(path, fallback = '') {
    if (!path) return fallback
    if (path.startsWith('http')) return path
    return IMAGE_BASE + path
}
