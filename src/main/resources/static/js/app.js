/**
 * 备忘录系统 - 通用工具库
 * 提供 Toast 通知、API 请求、日期格式化等通用功能
 */

// ==================== Toast 通知系统 ====================

/**
 * 显示一个 Toast 通知
 * @param {string} message  - 通知文本
 * @param {'success'|'error'|'warning'|'info'} type - 通知类型
 * @param {number} duration - 自动消失时间（毫秒），0 表示不自动消失
 */
function showToast(message, type = 'info', duration = 3000) {
  // 图标映射
  const icons = {
    success: '✓',
    error: '✕',
    warning: '⚠',
    info: 'ℹ'
  };

  // 创建 Toast 容器（若不存在）
  let container = document.querySelector('.toast-container');
  if (!container) {
    container = document.createElement('div');
    container.className = 'toast-container';
    document.body.appendChild(container);
  }

  // 创建 Toast 元素
  const toast = document.createElement('div');
  toast.className = `toast-notification ${type}`;
  toast.innerHTML = `
    <span class="toast-icon">${icons[type] || 'ℹ'}</span>
    <span class="toast-text">${escapeHtml(message)}</span>
    <button class="toast-close">&times;</button>
  `;

  // 关闭按钮事件
  toast.querySelector('.toast-close').addEventListener('click', () => {
    removeToast(toast);
  });

  container.appendChild(toast);

  // 自动消失
  if (duration > 0) {
    setTimeout(() => removeToast(toast), duration);
  }
}

/**
 * 移除 Toast 元素（带动画）
 */
function removeToast(toast) {
  if (toast.classList.contains('removing')) return;
  toast.classList.add('removing');
  setTimeout(() => {
    if (toast.parentNode) {
      toast.parentNode.removeChild(toast);
    }
  }, 300);
}

// ==================== API 请求工具 ====================

/**
 * 获取认证 Token
 */
function getToken() {
  return localStorage.getItem('token');
}

/**
 * 获取当前用户名
 */
function getUsername() {
  return localStorage.getItem('username') || '用户';
}

/**
 * 构造带认证头部的请求配置
 * @param {object} options - 额外的 fetch 配置
 */
function authFetchOptions(options = {}) {
  const headers = options.headers || {};
  return {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${getToken()}`,
      ...headers
    }
  };
}

/**
 * 封装的 API 请求（自动处理 JSON 解析和错误）
 * @param {string} url       - 请求地址
 * @param {object} options   - fetch 配置
 * @returns {Promise<object>} 解析后的 JSON 数据
 */
async function apiRequest(url, options = {}) {
  try {
    const response = await fetch(url, authFetchOptions(options));
    const data = await response.json();
    if (data.code !== 200) {
      showToast(data.msg || '操作失败', 'error');
      return null;
    }
    return data;
  } catch (error) {
    console.error('API 请求失败:', error);
    showToast('网络错误，请稍后重试', 'error');
    return null;
  }
}

/**
 * 检查登录状态，未登录则跳转
 */
function checkAuth() {
  if (!getToken()) {
    window.location.href = '/login';
    return false;
  }
  return true;
}

// ==================== 工具函数 ====================

/**
 * HTML 转义（防 XSS）
 */
function escapeHtml(text) {
  if (!text) return '';
  const div = document.createElement('div');
  div.textContent = text;
  return div.innerHTML;
}

/**
 * 日期格式化
 * @param {string|Date} date - 日期
 * @returns {string} 格式化的中文字符串
 */
function formatDate(date) {
  if (!date) return '';
  const d = new Date(date);
  return d.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
}

/**
 * 格式化日期为 yyyy-MM-dd
 */
function formatDateInput(date) {
  if (!date) return '';
  const d = new Date(date);
  return d.toISOString().split('T')[0];
}
