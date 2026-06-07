package com.gzeic.memosystem.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gzeic.memosystem.dto.DailySummaryResponse;
import com.gzeic.memosystem.dto.MemoPageRequest;
import com.gzeic.memosystem.dto.MemoRequest;
import com.gzeic.memosystem.entity.Memo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 备忘录服务接口
 *
 * 对应需求规格说明书中的 UC-03 至 UC-10
 *
 * @author jml
 * @description 针对表【memo】的数据库操作Service
 * @createDate 2026-02-04 09:40:13
 * @version V1.0 2026-06-07 新增分页查询和每日汇总方法
 */
public interface MemoService extends IService<Memo> {

    /**
     * 创建备忘录
     *
     * UC-03：新增备忘录
     *
     * @param request 备忘录请求DTO
     * @param userId 当前用户ID
     * @return 创建的备忘录
     */
    Memo createMemo(MemoRequest request, Long userId);

    /**
     * 更新备忘录
     *
     * UC-05：修改备忘录
     *
     * @param id 备忘录ID
     * @param request 备忘录请求DTO
     * @param userId 当前用户ID
     * @return 更新后的备忘录
     */
    Memo updateMemo(Long id, MemoRequest request, Long userId);

    /**
     * 删除备忘录
     *
     * UC-06：删除备忘录
     *
     * @param id 备忘录ID
     * @param userId 当前用户ID
     */
    void deleteMemo(Long id, Long userId);

    /**
     * 获取用户的所有备忘录（简单列表）
     *
     * @param userId 当前用户ID
     * @return 备忘录列表
     */
    List<Memo> getUserMemos(Long userId);

    /**
     * 获取备忘录详情
     *
     * @param id 备忘录ID
     * @param userId 当前用户ID
     * @return 备忘录
     */
    Memo getMemoById(Long id, Long userId);

    /**
     * 切换备忘录完成状态
     *
     * UC-07：切换完成状态（原toggle接口）
     *
     * @param id 备忘录ID
     * @param userId 当前用户ID
     * @return 更新后的备忘录
     */
    Memo toggleComplete(Long id, Long userId);

    /**
     * 根据状态获取备忘录列表
     *
     * @param userId 当前用户ID
     * @param isCompleted 完成状态
     * @return 备忘录列表
     */
    List<Memo> getMemosByStatus(Long userId, Boolean isCompleted);

    /**
     * 发送“到期提醒”消息（用于 MQ 演示/后续定时任务投递）
     *
     * @param id 备忘录ID
     * @param userId 当前用户ID
     */
    void sendDueNotify(Long id, Long userId);

    /**
     * 分页查询备忘录列表
     *
     * UC-04：分页查询备忘录列表
     *
     * 支持的筛选条件：
     * - 标题模糊查询
     * - 优先级筛选（HIGH/MEDIUM/LOW）
     * - 完成状态筛选
     * - 截止日期范围筛选
     * - 自定义排序
     *
     * @param request 分页查询请求参数
     * @param userId 当前用户ID
     * @return 分页结果
     */
    IPage<Memo> pageMemos(MemoPageRequest request, Long userId);

    /**
     * 设置备忘录完成状态
     *
     * UC-07：切换完成状态（新版接口）
     *
     * @param id 备忘录ID
     * @param completed 是否完成
     * @param userId 当前用户ID
     * @return 更新后的备忘录
     */
    Memo setCompleted(Long id, Boolean completed, Long userId);

    /**
     * 获取每日待办汇总统计
     *
     * UC-08：每日待办汇总统计
     *
     * 统计指标：
     * - todayTotal: 今日待办总数
     * - completed: 已完成数
     * - uncompleted: 未完成数
     * - highPriority: 高优先级数
     * - overdue: 逾期数
     *
     * @param userId 当前用户ID
     * @return 每日汇总统计
     */
    DailySummaryResponse getDailySummary(Long userId);
}