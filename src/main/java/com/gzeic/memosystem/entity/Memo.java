package com.gzeic.memosystem.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 备忘录表实体类
 *
 * 说明：
 * - 这是 SaaS 多租户系统的业务核心表，每条备忘录必须属于某个租户（tenantId）
 * - 同时每条备忘录属于某个用户（userId）
 *
 * @author jml
 * @TableName memo
 */
@Data
@TableName(value = "memo")
public class Memo {

    /**
     * 备忘录主键（自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 租户 ID（多租户隔离的核心字段）
     *
     * 说明：
     * - 查询时 MP 租户插件会自动追加 tenant_id 条件
     * - 新增时也会自动写入 tenant_id（来自 TenantContext / JWT / 请求头等）
     */
    private Long tenantId;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 所属用户 ID
     */
    private Long userId;

    /**
     * 是否已完成
     */
    private Boolean isCompleted;

    /**
     * 是否已发送过"到期提醒"
     *
     * 说明：
     * - 定时任务扫描到期备忘录后，会发送 MQ 消息
     * - 为了避免重复发送，我们用该字段做幂等控制
     */
    private Boolean isNotified;

    /**
     * 优先级（数值越大表示越重要，具体规则可由业务定义）
     * 1=HIGH, 2=MEDIUM, 3=LOW
     */
    private Integer priority;

    /**
     * 到期时间
     */
    private LocalDateTime dueDate;

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