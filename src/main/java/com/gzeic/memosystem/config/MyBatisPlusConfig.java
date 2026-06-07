package com.gzeic.memosystem.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.gzeic.memosystem.tenant.TenantContext;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置类：启用 SaaS 多租户隔离（tenant_id）
 *
 * 核心效果：
 * - 对所有“走 MyBatis-Plus 执行的 SQL”（SELECT/UPDATE/DELETE）自动追加租户条件
 * - 例如：SELECT * FROM memo  =>  SELECT * FROM memo WHERE tenant_id = ?
 *
 * 说明：
 * - tenantId 的来源不在 MyBatis 层处理，而是由上层（Filter/JWT/网关）写入 TenantContext
 * - 本配置只负责“从 TenantContext 取值，并告诉 MP tenant_id 列名是什么”
 */
@Configuration
public class MyBatisPlusConfig {

    /**
     * 注册 MyBatis-Plus 拦截器总入口
     *
     * 注意：
     * - MybatisPlusInterceptor 是 MP 3.4+ 的新插件体系（多个 InnerInterceptor 组成链）
     * - 这里我们添加 TenantLineInnerInterceptor，实现“逻辑租户隔离”
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 1) 添加“多租户行级拦截器”
        //    它会在 SQL 解析阶段，为目标表自动拼接 tenant_id 条件
        interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(new TenantLineHandler() {

            /**
             * 返回当前租户 ID 的表达式对象（JSqlParser 需要 Expression）
             *
             * 关键点：
             * - tenantId 来自 TenantContext（ThreadLocal），由 TenantHeaderFilter 在请求入口写入
             * - 如果 tenantId 为空，通常表示“当前请求未注入租户信息”
             *   为了强制隔离，这里返回 -1（一般数据库不会存在该租户），从而避免查出任何真实数据
             */
            @Override
            public Expression getTenantId() {
                Long tenantId = TenantContext.getTenantId();
                if (tenantId == null) {
                    return new LongValue(-1L);
                }
                return new LongValue(tenantId);
            }

            /**
             * 指定租户列名：tenant_id
             *
             * 要求：
             * - 所有需要隔离的业务表都应包含 tenant_id 字段
             */
            @Override
            public String getTenantIdColumn() {
                return "tenant_id";
            }

            /**
             * 是否忽略某些表的租户过滤
             *
             * 本项目要求强制 tenant_id 隔离，因此默认不忽略任何表。
             * 如果后续确实存在“公共表”（如省市字典表），可在这里按表名放行。
             */
            @Override
            public boolean ignoreTable(String tableName) {
                return false;
            }
        }));

        return interceptor;
    }
}

