package com.gzeic.memosystem.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 用户表实体类
 *
 * 说明：
 * - 这是一个 SaaS 多租户系统，因此每条用户数据都必须属于某个租户（tenantId）
 * - 同一个用户名在不同租户下允许重复（用 tenantId + username 作为联合唯一）
 *
 * @author jml
 * @TableName user
 */
@Data
@TableName(value = "user")
public class User {

    /**
     * 用户主键（自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 租户 ID（多租户隔离的核心字段）
     *
     * 注意：
     * - MyBatis-Plus 的 TenantLineInnerInterceptor 会自动在 SQL 中追加 tenant_id 条件
     * - 新增数据时也会自动写入 tenant_id（来自 TenantContext / JWT / 请求头等）
     */
    private Long tenantId;

    /**
     * 用户名（在同一个租户内唯一）
     */
    private String username;

    /**
     * 密码（数据库中保存加密后的密文，如 BCrypt）
     */
    private String password;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 创建时间（由 MyBatis-Plus 自动填充）
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间（由 MyBatis-Plus 自动填充）
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}