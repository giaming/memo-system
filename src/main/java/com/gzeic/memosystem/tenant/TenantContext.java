package com.gzeic.memosystem.tenant;

/**
 * 租户上下文（ThreadLocal 版）
 *
 * 设计目的：
 * - 在一次 HTTP 请求的处理线程内，全局保存当前请求所属的 tenantId
 * - 供 MyBatis-Plus 多租户拦截器随时读取，从而自动拼接 tenant_id 条件
 *
 * 为什么用 ThreadLocal：
 * - Spring MVC 默认一个请求由一个线程处理（线程池复用线程，但同一请求内线程一致）
 * - 用 ThreadLocal 可以做到“同线程可见、跨线程隔离”，实现最简单的上下文传递
 *
 * 重要注意：
 * - 线程会被线程池复用，所以请求结束必须 clear()，否则会发生“租户串线”的严重安全问题
 */
public final class TenantContext {

    /**
     * 用 ThreadLocal 存储 tenantId（每个线程一份副本，互不影响）
     * 这里使用 Long 是为了贴合数据库 tenant_id 常见类型（bigint）
     */
    private static final ThreadLocal<Long> TENANT_ID_HOLDER = new ThreadLocal<>();

    /**
     * 工具类不允许被实例化（避免误用）
     */
    private TenantContext() {
    }

    /**
     * 设置当前线程的租户 ID
     *
     * @param tenantId 租户 ID（建议来自请求头 / JWT / 网关注入等）
     */
    public static void setTenantId(Long tenantId) {
        TENANT_ID_HOLDER.set(tenantId);
    }

    /**
     * 获取当前线程的租户 ID
     *
     * @return tenantId；如果未设置则返回 null
     */
    public static Long getTenantId() {
        return TENANT_ID_HOLDER.get();
    }

    /**
     * 清理当前线程的租户 ID
     *
     * 必须在请求结束时调用：
     * - 因为 Web 容器线程来自线程池，线程会被重复使用
     * - 如果不 remove，下一个请求可能读到上一个请求残留的 tenantId，造成越权/数据泄露
     */
    public static void clear() {
        TENANT_ID_HOLDER.remove();
    }
}

