package com.gzeic.memosystem.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 多租户配置（教学版）
 *
 * 说明：
 * - 真正的 SaaS 系统通常会有“租户表”，或者由网关/认证中心管理租户
 * - 教学 Demo 为了便于落地与测试，我们用配置文件维护一个“租户列表”
 *
 * 对应 application.yaml：
 * app:
 *   tenants:
 *     - 1001
 *     - 1002
 */
@Data
@Component
@ConfigurationProperties(prefix = "app")
public class TenantProperties {

    /**
     * 系统已开通的租户 ID 列表（定时任务需要逐个租户执行）
     */
    private List<Long> tenants;
}

