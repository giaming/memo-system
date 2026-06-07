package com.gzeic.memosystem.tenant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 租户请求头过滤器：
 * - 从 HTTP Header 读取 X-Tenant-Id
 * - 写入 TenantContext(ThreadLocal)，供后续 MyBatis-Plus 租户插件读取
 * - 请求结束（finally）必须清理 TenantContext，防止线程池复用导致串租户
 *
 * 约定：
 * - Header 名固定为：X-Tenant-Id
 * - 值使用 Long（与数据库 tenant_id 常用 bigint 对齐）
 */
public class TenantHeaderFilter extends OncePerRequestFilter {

    /**
     * 统一管理 Header 名，避免硬编码散落各处
     */
    public static final String TENANT_HEADER = "X-Tenant-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1) 读取请求头中的租户 ID（如果没有该头，则 tenantIdStr 为 null）
        String tenantIdStr = request.getHeader(TENANT_HEADER);

        try {
            // 2) 若请求头携带租户 ID，则解析并写入 ThreadLocal
            //    注意：这里不做“默认租户”兜底，避免无租户请求误入某个固定租户
            if (tenantIdStr != null && !tenantIdStr.isBlank()) {
                Long tenantId = Long.parseLong(tenantIdStr.trim());
                TenantContext.setTenantId(tenantId);
            }

            // 3) 放行请求：后续 Controller / Service / MyBatis 执行期间都可读取 TenantContext
            filterChain.doFilter(request, response);
        } finally {
            // 4) 关键：无论成功/异常，都必须清理 ThreadLocal
            //    否则线程池复用线程时会“带着上一次请求的 tenantId”，引发严重越权
            TenantContext.clear();
        }
    }
}

