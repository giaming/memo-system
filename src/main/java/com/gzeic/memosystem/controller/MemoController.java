package com.gzeic.memosystem.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gzeic.memosystem.dto.DailySummaryResponse;
import com.gzeic.memosystem.dto.MemoPageRequest;
import com.gzeic.memosystem.dto.MemoRequest;
import com.gzeic.memosystem.entity.Memo;
import com.gzeic.memosystem.service.MemoService;
import com.gzeic.memosystem.util.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 备忘录管理控制器
 *
 * 对应需求规格说明书中的 UC-03 至 UC-10
 *
 * 接口清单：
 * - POST /api/memo - 创建备忘录（UC-03）
 * - GET /api/memo/page - 分页查询备忘录列表（UC-04）
 * - PUT /api/memo/{id} - 修改备忘录（UC-05）
 * - DELETE /api/memo/{id} - 删除备忘录（UC-06）
 * - PATCH /api/memo/{id}/status - 设置完成状态（UC-07）
 * - GET /api/memo/daily-summary - 每日待办汇总（UC-08）
 * - GET /api/memo/{id} - 获取备忘录详情
 * - GET /api/memo - 获取用户所有备忘录
 * - PUT /api/memo/{id}/toggle - 切换完成状态（兼容旧接口）
 *
 * @author jml
 * @version V1.1 2026-06-07 新增分页查询和每日汇总接口
 */
@RestController
@RequestMapping("/api/memo")
@RequiredArgsConstructor
@Tag(name = "备忘录管理接口", description = "包含创建、更新、删除、查询、分页、汇总等相关接口")
public class MemoController {

    private final MemoService memoService;

    /**
     * 创建备忘录
     *
     * UC-03：新增备忘录
     *
     * 请求示例：
     * POST /api/memo
     * {
     *   "title": "完成项目报告",
     *   "content": "需要在下周五前提交",
     *   "priority": "HIGH",
     *   "dueDate": "2026-06-15"
     * }
     */
    @PostMapping
    @Operation(summary = "创建备忘录", description = "创建新的备忘录，支持设置优先级和截止日期")
    public Result<Memo> createMemo(
            @Valid @RequestBody MemoRequest request,
            @RequestAttribute("userId") Long userId) {
        Memo memo = memoService.createMemo(request, userId);
        return Result.success("创建成功", memo);
    }

    /**
     * 分页查询备忘录列表
     *
     * UC-04：分页查询备忘录列表
     *
     * 支持的筛选条件：
     * - title: 标题模糊查询
     * - priority: 优先级筛选（HIGH/MEDIUM/LOW）
     * - completed: 完成状态筛选（true/false）
     * - startDueDate/endDueDate: 截止日期范围筛选
     * - orderBy: 排序字段
     * - sort: 排序方向（asc/desc）
     *
     * 请求示例：
     * GET /api/memo/page?pageNum=1&pageSize=10&title=报告&priority=HIGH&completed=false&orderBy=dueDate&sort=asc
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询备忘录", description = "分页查询当前用户的备忘录列表，支持多条件筛选和排序")
    public Result<IPage<Memo>> pageMemos(
            @Parameter(description = "分页查询请求参数") MemoPageRequest request,
            @RequestAttribute("userId") Long userId) {
        IPage<Memo> page = memoService.pageMemos(request, userId);
        return Result.success(page);
    }

    /**
     * 获取当前用户的所有备忘录
     */
    @GetMapping
    @Operation(summary = "获取当前用户的所有备忘录", description = "获取当前用户的所有备忘录（不分页）")
    public Result<List<Memo>> getUserMemos(@RequestAttribute("userId") Long userId) {
        List<Memo> memos = memoService.getUserMemos(userId);
        return Result.success(memos);
    }

    /**
     * 获取备忘录详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取备忘录详情", description = "根据ID获取备忘录详情")
    public Result<Memo> getMemoById(
            @PathVariable Long id,
            @RequestAttribute("userId") Long userId) {
        Memo memo = memoService.getMemoById(id, userId);
        return Result.success(memo);
    }

    /**
     * 修改备忘录
     *
     * UC-05：修改备忘录
     *
     * 请求示例：
     * PUT /api/memo/1
     * {
     *   "title": "更新后的标题",
     *   "content": "更新后的内容",
     *   "priority": "MEDIUM",
     *   "dueDate": "2026-06-20"
     * }
     */
    @PutMapping("/{id}")
    @Operation(summary = "修改备忘录", description = "根据ID修改备忘录")
    public Result<Memo> updateMemo(
            @PathVariable Long id,
            @Valid @RequestBody MemoRequest request,
            @RequestAttribute("userId") Long userId) {
        Memo memo = memoService.updateMemo(id, request, userId);
        return Result.success("修改成功", memo);
    }

    /**
     * 删除备忘录
     *
     * UC-06：删除备忘录
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除备忘录", description = "根据ID删除备忘录")
    public Result<Void> deleteMemo(
            @PathVariable Long id,
            @RequestAttribute("userId") Long userId) {
        memoService.deleteMemo(id, userId);
        return Result.success("删除成功", null);
    }

    /**
     * 设置备忘录完成状态
     *
     * UC-07：切换完成状态（新版接口）
     *
     * 请求示例：
     * PATCH /api/memo/1/status?completed=true
     */
    @PatchMapping("/{id}/status")
    @Operation(summary = "设置备忘录完成状态", description = "设置备忘录的完成状态（true=已完成，false=未完成）")
    public Result<Memo> setCompleted(
            @PathVariable Long id,
            @RequestParam Boolean completed,
            @RequestAttribute("userId") Long userId) {
        Memo memo = memoService.setCompleted(id, completed, userId);
        return Result.success("状态已更新", memo);
    }

    /**
     * 切换备忘录完成状态（兼容旧接口）
     *
     * @deprecated 建议使用 PATCH /api/memo/{id}/status 接口
     */
    @Deprecated
    @PutMapping("/{id}/toggle")
    @Operation(summary = "切换备忘录完成状态（兼容旧接口）", description = "切换备忘录的完成状态，建议使用 PATCH /api/memo/{id}/status 接口")
    public Result<Memo> toggleComplete(
            @PathVariable Long id,
            @RequestAttribute("userId") Long userId) {
        Memo memo = memoService.toggleComplete(id, userId);
        return Result.success("状态已切换", memo);
    }

    /**
     * 根据完成状态获取备忘录
     */
    @GetMapping("/status/{isCompleted}")
    @Operation(summary = "根据完成状态获取备忘录", description = "根据完成状态筛选备忘录")
    public Result<List<Memo>> getMemosByStatus(
            @PathVariable Boolean isCompleted,
            @RequestAttribute("userId") Long userId) {
        List<Memo> memos = memoService.getMemosByStatus(userId, isCompleted);
        return Result.success(memos);
    }

    /**
     * 获取每日待办汇总统计
     *
     * UC-08：每日待办汇总统计
     *
     * 返回示例：
     * {
     *   "code": 200,
     *   "data": {
     *     "todayTotal": 8,
     *     "completed": 3,
     *     "uncompleted": 5,
     *     "highPriority": 2,
     *     "overdue": 1,
     *     "summaryDate": "2026-06-07"
     *   }
     * }
     */
    @GetMapping("/daily-summary")
    @Operation(summary = "获取每日待办汇总", description = "获取当前用户的每日待办汇总统计")
    public Result<DailySummaryResponse> getDailySummary(@RequestAttribute("userId") Long userId) {
        DailySummaryResponse summary = memoService.getDailySummary(userId);
        return Result.success(summary);
    }

    /**
     * 发送到期提醒消息（MQ 演示用）
     */
    @PostMapping("/{id}/notify")
    @Operation(summary = "发送到期提醒消息", description = "把指定备忘录的提醒事件发送到 RabbitMQ（用于演示异步通知）")
    public Result<Void> sendDueNotify(
            @PathVariable Long id,
            @RequestAttribute("userId") Long userId) {
        memoService.sendDueNotify(id, userId);
        return Result.success("已发送提醒消息", null);
    }
}