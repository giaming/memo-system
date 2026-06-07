package com.gzeic.memosystem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 每日待办汇总响应DTO
 *
 * 对应需求规格说明书 UC-08：每日待办汇总统计
 *
 * 统计指标说明：
 * - todayTotal: 今日待办总数（截止日期为今天及以前且未完成的备忘录 + 无截止日期今天创建的未完成备忘录）
 * - completed: 已完成数
 * - uncompleted: 未完成数
 * - highPriority: 高优先级数
 * - overdue: 逾期数
 *
 * @author 系统分析组
 * @version V1.0 2026-06-07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "每日待办汇总响应")
public class DailySummaryResponse {

    @Schema(description = "今日待办总数")
    private Integer todayTotal;

    @Schema(description = "已完成数")
    private Integer completed;

    @Schema(description = "未完成数")
    private Integer uncompleted;

    @Schema(description = "高优先级数（优先级为HIGH的未完成备忘录）")
    private Integer highPriority;

    @Schema(description = "逾期数（截止日期已过且未完成的备忘录）")
    private Integer overdue;

    @Schema(description = "统计日期")
    private String summaryDate;
}