package com.gzeic.memosystem.dto;

import com.gzeic.memosystem.enums.PriorityEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * 备忘录创建/更新请求DTO
 *
 * 对应需求规格说明书 UC-03（新增）和 UC-05（修改）
 *
 * 字段说明：
 * - title: 必填，最大100字符
 * - content: 可选，最大500字符（需求文档要求）
 * - priority: 枚举值 HIGH/MEDIUM/LOW，默认 MEDIUM
 * - dueDate: 可选的截止日期
 *
 * @author 系统分析组
 * @version V1.0 2026-06-07
 */
@Data
@Schema(description = "备忘录创建/更新请求")
public class MemoRequest {

    /**
     * 标题（必填）
     */
    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题长度不能超过100个字符")
    @Schema(description = "备忘录标题", example = "完成项目报告")
    private String title;

    /**
     * 内容（可选）
     */
    @Size(max = 500, message = "内容长度不能超过500个字符")
    @Schema(description = "备忘录内容", example = "需要在下周五前提交")
    private String content;

    /**
     * 优先级（枚举：HIGH/MEDIUM/LOW，默认 MEDIUM）
     *
     * 需求文档 UC-03 要求：
     * - 枚举：HIGH/MEDIUM/LOW
     * - 默认 MEDIUM
     */
    @Schema(description = "优先级，HIGH/MEDIUM/LOW，默认 MEDIUM", example = "HIGH")
    private String priority;

    /**
     * 截止日期（可选）
     *
     * 需求文档业务规则 BR-02：
     * - 截止日期如果为空，则该备忘录不参与到期提醒
     */
    @Schema(description = "截止日期，格式：yyyy-MM-dd", example = "2026-06-15")
    private LocalDate dueDate;

    /**
     * 获取优先级枚举
     *
     * @return PriorityEnum，默认返回 MEDIUM
     */
    public PriorityEnum getPriorityEnum() {
        return PriorityEnum.fromName(this.priority);
    }

    /**
     * 获取优先级的数值（用于数据库存储）
     *
     * @return 优先级数值：1=HIGH, 2=MEDIUM, 3=LOW
     */
    public Integer getPriorityValue() {
        return getPriorityEnum().getValue();
    }
}