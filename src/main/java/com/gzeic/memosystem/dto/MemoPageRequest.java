package com.gzeic.memosystem.dto;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 备忘录分页查询请求DTO
 *
 * 对应需求规格说明书 UC-04：分页查询备忘录列表
 * 支持按标题模糊查询、优先级筛选、完成状态筛选、截止日期范围筛选
 *
 * @author 系统分析组
 * @version V1.0 2026-06-07
 */
@Data
public class MemoPageRequest {

    /**
     * 页码（默认1）
     */
    @Parameter(description = "页码，默认1")
    private Integer pageNum = 1;

    /**
     * 每页条数（默认10，最大100）
     */
    @Parameter(description = "每页条数，默认10，最大100")
    private Integer pageSize = 10;

    /**
     * 标题（可选，用于模糊查询）
     */
    @Parameter(description = "标题关键字，用于模糊查询")
    private String title;

    /**
     * 优先级筛选（可选，HIGH/MEDIUM/LOW）
     */
    @Parameter(description = "优先级筛选，HIGH/MEDIUM/LOW")
    private String priority;

    /**
     * 完成状态筛选（可选，true=已完成，false=未完成）
     */
    @Parameter(description = "完成状态筛选，true=已完成，false=未完成")
    private Boolean completed;

    /**
     * 截止日期范围-开始日期
     */
    @Parameter(description = "截止日期范围-开始日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDueDate;

    /**
     * 截止日期范围-结束日期
     */
    @Parameter(description = "截止日期范围-结束日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDueDate;

    /**
     * 排序字段（如 createTime、dueDate）
     */
    @Parameter(description = "排序字段，默认为 createTime")
    private String orderBy = "createTime";

    /**
     * 排序方向（asc/desc）
     */
    @Parameter(description = "排序方向，asc 或 desc，默认为 desc")
    private String sort = "desc";

    /**
     * 校验并修正分页参数
     */
    public void validate() {
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }
        if (pageSize > 100) {
            pageSize = 100;
        }
        if (sort == null || (!sort.equalsIgnoreCase("asc") && !sort.equalsIgnoreCase("desc"))) {
            sort = "desc";
        }
    }
}