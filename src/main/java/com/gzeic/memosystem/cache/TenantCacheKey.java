package com.gzeic.memosystem.cache;

import com.gzeic.memosystem.tenant.TenantContext;

import java.util.StringJoiner;

/**
 * 多租户缓存 Key 生成工具（给 Spring Cache 的 SpEL 使用）
 *
 * 目标：
 * - 让缓存 key 自动带上租户前缀，避免不同租户的数据在缓存中互相污染
 *
 * 使用方式（示例）：
 * - @Cacheable(cacheNames = "memo", key = "T(com.gzeic.memosystem.cache.TenantCacheKey).key('detail', #id, #userId)")
 *
 * 生成结果（示例）：
 * - tenant:1001:detail:123:88
 */
public final class TenantCacheKey {

    private TenantCacheKey() {
    }

    /**
     * 生成带 tenant 前缀的缓存 key
     *
     * @param namespace 业务命名空间（例如 memo:list / memo:detail）
     * @param parts     参与拼接的业务参数（通常是 id、userId、分页参数等）
     * @return 最终缓存 key（字符串）
     */
    public static String key(String namespace, Object... parts) {
        Long tenantId = TenantContext.getTenantId();

        StringJoiner joiner = new StringJoiner(":");
        joiner.add("tenant");
        joiner.add(tenantId == null ? "null" : String.valueOf(tenantId));
        joiner.add(namespace);

        if (parts != null) {
            for (Object part : parts) {
                joiner.add(part == null ? "null" : String.valueOf(part));
            }
        }

        return joiner.toString();
    }
}

