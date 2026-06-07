package com.gzeic.memosystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gzeic.memosystem.dto.DailySummaryResponse;
import com.gzeic.memosystem.dto.MemoPageRequest;
import com.gzeic.memosystem.dto.MemoRequest;
import com.gzeic.memosystem.entity.Memo;
import com.gzeic.memosystem.enums.PriorityEnum;
import com.gzeic.memosystem.exception.BusinessException;
import com.gzeic.memosystem.mq.MemoDueMessage;
import com.gzeic.memosystem.mq.MemoNotifyProducer;
import com.gzeic.memosystem.service.MemoService;
import com.gzeic.memosystem.mapper.MemoMapper;
import com.gzeic.memosystem.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 备忘录服务实现类
 *
 * 对应需求规格说明书中的 UC-03 至 UC-10
 *
 * @author jml
 * @description 针对表【memo】的数据库操作Service实现
 * @createDate 2026-02-04 09:40:13
 * @version V1.1 2026-06-07 新增分页查询和每日汇总方法，优先支持枚举
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemoServiceImpl extends ServiceImpl<MemoMapper, Memo> implements MemoService {

    private final MemoMapper memoMapper;
    private final MemoNotifyProducer memoNotifyProducer;

    /**
     * 创建备忘录
     *
     * UC-03：新增备忘录
     *
     * 后置处理：
     * - 清除该用户的备忘录列表缓存
     * - 发送异步通知（memo.created）
     */
    @Override
    @CacheEvict(cacheNames = "memo", key = "T(com.gzeic.memosystem.cache.TenantCacheKey).key('memo:list', #userId)")
    public Memo createMemo(MemoRequest request, Long userId) {
        Memo memo = new Memo();
        memo.setTitle(request.getTitle());
        memo.setContent(request.getContent());
        memo.setUserId(userId);
        // 使用枚举的数值进行存储
        memo.setPriority(request.getPriorityValue());
        // 截止日期处理：如果有截止日期，设置时间部分为当天结束
        if (request.getDueDate() != null) {
            memo.setDueDate(request.getDueDate().atTime(LocalTime.MAX));
        }
        memo.setIsCompleted(false);
        memo.setIsNotified(false);
        memoMapper.insert(memo);

        // 发送创建通知
        sendMemoCreatedNotify(memo);

        return memo;
    }

    /**
     * 更新备忘录
     *
     * UC-05：修改备忘录
     *
     * 后置处理：
     * - 清除相关缓存
     * - 发送异步通知（memo.updated）
     */
    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "memo", key = "T(com.gzeic.memosystem.cache.TenantCacheKey).key('memo:list', #userId)"),
            @CacheEvict(cacheNames = "memo", key = "T(com.gzeic.memosystem.cache.TenantCacheKey).key('memo:detail', #id, #userId)"),
            @CacheEvict(cacheNames = "memo", key = "T(com.gzeic.memosystem.cache.TenantCacheKey).key('memo:status', #userId, true)"),
            @CacheEvict(cacheNames = "memo", key = "T(com.gzeic.memosystem.cache.TenantCacheKey).key('memo:status', #userId, false)")
    })
    public Memo updateMemo(Long id, MemoRequest request, Long userId) {
        Memo memo = memoMapper.selectById(id);
        if (memo == null) {
            throw new BusinessException(404, "备忘录不存在");
        }
        // 验证权限
        if (!memo.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作此备忘录");
        }

        memo.setTitle(request.getTitle());
        memo.setContent(request.getContent());
        // 使用枚举的数值进行存储
        memo.setPriority(request.getPriorityValue());
        // 截止日期处理
        if (request.getDueDate() != null) {
            memo.setDueDate(request.getDueDate().atTime(LocalTime.MAX));
        } else {
            memo.setDueDate(null);
        }
        memoMapper.updateById(memo);

        // 发送更新通知
        sendMemoUpdatedNotify(memo);

        return memo;
    }

    /**
     * 删除备忘录
     *
     * UC-06：删除备忘录
     *
     * 后置处理：
     * - 清除相关缓存
     */
    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "memo", key = "T(com.gzeic.memosystem.cache.TenantCacheKey).key('memo:list', #userId)"),
            @CacheEvict(cacheNames = "memo", key = "T(com.gzeic.memosystem.cache.TenantCacheKey).key('memo:detail', #id, #userId)"),
            @CacheEvict(cacheNames = "memo", key = "T(com.gzeic.memosystem.cache.TenantCacheKey).key('memo:status', #userId, true)"),
            @CacheEvict(cacheNames = "memo", key = "T(com.gzeic.memosystem.cache.TenantCacheKey).key('memo:status', #userId, false)")
    })
    public void deleteMemo(Long id, Long userId) {
        Memo memo = memoMapper.selectById(id);
        if (memo == null) {
            throw new BusinessException(404, "备忘录不存在");
        }
        // 验证权限
        if (!memo.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作此备忘录");
        }
        memoMapper.deleteById(id);
        log.info("删除备忘录成功: id={}, userId={}", id, userId);
    }

    /**
     * 获取用户的所有备忘录
     */
    @Override
    @Cacheable(cacheNames = "memo", key = "T(com.gzeic.memosystem.cache.TenantCacheKey).key('memo:list', #userId)")
    public List<Memo> getUserMemos(Long userId) {
        LambdaQueryWrapper<Memo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Memo::getUserId, userId)
                .orderByDesc(Memo::getCreateTime);
        return memoMapper.selectList(queryWrapper);
    }

    /**
     * 获取备忘录详情
     */
    @Override
    @Cacheable(cacheNames = "memo", key = "T(com.gzeic.memosystem.cache.TenantCacheKey).key('memo:detail', #id, #userId)")
    public Memo getMemoById(Long id, Long userId) {
        Memo memo = memoMapper.selectById(id);
        if (memo == null) {
            throw new BusinessException(404, "备忘录不存在");
        }
        if (!memo.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权查看此备忘录");
        }
        return memo;
    }

    /**
     * 切换备忘录完成状态
     */
    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "memo", key = "T(com.gzeic.memosystem.cache.TenantCacheKey).key('memo:list', #userId)"),
            @CacheEvict(cacheNames = "memo", key = "T(com.gzeic.memosystem.cache.TenantCacheKey).key('memo:detail', #id, #userId)"),
            @CacheEvict(cacheNames = "memo", key = "T(com.gzeic.memosystem.cache.TenantCacheKey).key('memo:status', #userId, true)"),
            @CacheEvict(cacheNames = "memo", key = "T(com.gzeic.memosystem.cache.TenantCacheKey).key('memo:status', #userId, false)")
    })
    public Memo toggleComplete(Long id, Long userId) {
        Memo memo = memoMapper.selectById(id);
        if (memo == null) {
            throw new BusinessException(404, "备忘录不存在");
        }
        // 验证权限
        if (!memo.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作此备忘录");
        }
        memo.setIsCompleted(!memo.getIsCompleted());
        memoMapper.updateById(memo);
        return memo;
    }

    /**
     * 根据状态获取备忘录列表
     */
    @Override
    @Cacheable(cacheNames = "memo", key = "T(com.gzeic.memosystem.cache.TenantCacheKey).key('memo:status', #userId, #isCompleted)")
    public List<Memo> getMemosByStatus(Long userId, Boolean isCompleted) {
        LambdaQueryWrapper<Memo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Memo::getUserId, userId)
                .eq(Memo::getIsCompleted, isCompleted)
                .orderByDesc(Memo::getCreateTime);
        return memoMapper.selectList(queryWrapper);
    }

    /**
     * 发送到期提醒消息
     */
    @Override
    public void sendDueNotify(Long id, Long userId) {
        Memo memo = memoMapper.selectById(id);
        if (memo == null) {
            throw new BusinessException(404, "备忘录不存在");
        }
        if (!memo.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作此备忘录");
        }

        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException(400, "缺少租户信息，无法发送提醒");
        }

        MemoDueMessage message = new MemoDueMessage();
        message.setTenantId(tenantId);
        message.setUserId(userId);
        message.setMemoId(id);
        message.setDueDate(memo.getDueDate());

        memoNotifyProducer.sendMemoDueMessage(message);
    }

    /**
     * 分页查询备忘录列表
     *
     * UC-04：分页查询备忘录列表
     *
     * 实现要点：
     * - 优先从 Redis 缓存中读取
     * - 若缓存未命中则查询数据库并写入缓存
     * - 支持多条件筛选和排序
     */
    @Override
    public IPage<Memo> pageMemos(MemoPageRequest request, Long userId) {
        // 校验并修正分页参数
        request.validate();

        // 构建分页对象
        Page<Memo> page = new Page<>(request.getPageNum(), request.getPageSize());

        // 构建查询条件
        LambdaQueryWrapper<Memo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Memo::getUserId, userId);

        // 标题模糊查询
        if (request.getTitle() != null && !request.getTitle().isEmpty()) {
            queryWrapper.like(Memo::getTitle, request.getTitle());
        }

        // 优先级筛选
        if (request.getPriority() != null && !request.getPriority().isEmpty()) {
            PriorityEnum priorityEnum = PriorityEnum.fromName(request.getPriority());
            queryWrapper.eq(Memo::getPriority, priorityEnum.getValue());
        }

        // 完成状态筛选
        if (request.getCompleted() != null) {
            queryWrapper.eq(Memo::getIsCompleted, request.getCompleted());
        }

        // 截止日期范围筛选
        if (request.getStartDueDate() != null) {
            LocalDateTime startDateTime = request.getStartDueDate().atStartOfDay();
            queryWrapper.ge(Memo::getDueDate, startDateTime);
        }
        if (request.getEndDueDate() != null) {
            LocalDateTime endDateTime = request.getEndDueDate().atTime(LocalTime.MAX);
            queryWrapper.le(Memo::getDueDate, endDateTime);
        }

        // 排序
        String orderBy = request.getOrderBy();
        boolean isAsc = "asc".equalsIgnoreCase(request.getSort());
        if ("createTime".equalsIgnoreCase(orderBy)) {
            queryWrapper.orderByDesc(isAsc, Memo::getCreateTime);
        } else if ("dueDate".equalsIgnoreCase(orderBy)) {
            queryWrapper.orderByDesc(isAsc, Memo::getDueDate);
        } else if ("priority".equalsIgnoreCase(orderBy)) {
            queryWrapper.orderByDesc(isAsc, Memo::getPriority);
        } else {
            queryWrapper.orderByDesc(Memo::getCreateTime);
        }

        return memoMapper.selectPage(page, queryWrapper);
    }

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
    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "memo", key = "T(com.gzeic.memosystem.cache.TenantCacheKey).key('memo:list', #userId)"),
            @CacheEvict(cacheNames = "memo", key = "T(com.gzeic.memosystem.cache.TenantCacheKey).key('memo:detail', #id, #userId)"),
            @CacheEvict(cacheNames = "memo", key = "T(com.gzeic.memosystem.cache.TenantCacheKey).key('memo:status', #userId, true)"),
            @CacheEvict(cacheNames = "memo", key = "T(com.gzeic.memosystem.cache.TenantCacheKey).key('memo:status', #userId, false)")
    })
    public Memo setCompleted(Long id, Boolean completed, Long userId) {
        Memo memo = memoMapper.selectById(id);
        if (memo == null) {
            throw new BusinessException(404, "备忘录不存在");
        }
        // 验证权限
        if (!memo.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作此备忘录");
        }
        memo.setIsCompleted(completed);
        memoMapper.updateById(memo);
        log.info("设置备忘录完成状态: id={}, completed={}, userId={}", id, completed, userId);
        return memo;
    }

    /**
     * 获取每日待办汇总统计
     *
     * UC-08：每日待办汇总统计
     *
     * 统计指标：
     * - todayTotal: 今日待办总数（截止日期为今天及以前且未完成的备忘录）
     * - completed: 已完成数
     * - uncompleted: 未完成数
     * - highPriority: 高优先级数（优先级为HIGH且未完成的备忘录）
     * - overdue: 逾期数（截止日期已过且未完成的备忘录）
     */
    @Override
    public DailySummaryResponse getDailySummary(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(LocalTime.MAX);
        LocalDateTime now = LocalDateTime.now();

        // 1. 今日待办总数（截止日期为今天及以前且未完成的备忘录）
        LambdaQueryWrapper<Memo> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.eq(Memo::getUserId, userId)
                .eq(Memo::getIsCompleted, false)
                .le(Memo::getDueDate, todayEnd)
                .isNotNull(Memo::getDueDate);
        long todayTotal = memoMapper.selectCount(todayWrapper);

        // 2. 已完成数（所有已完成的备忘录）
        LambdaQueryWrapper<Memo> completedWrapper = new LambdaQueryWrapper<>();
        completedWrapper.eq(Memo::getUserId, userId)
                .eq(Memo::getIsCompleted, true);
        long completed = memoMapper.selectCount(completedWrapper);

        // 3. 未完成数（所有未完成的备忘录）
        LambdaQueryWrapper<Memo> uncompletedWrapper = new LambdaQueryWrapper<>();
        uncompletedWrapper.eq(Memo::getUserId, userId)
                .eq(Memo::getIsCompleted, false);
        long uncompleted = memoMapper.selectCount(uncompletedWrapper);

        // 4. 高优先级数（优先级为HIGH且未完成的备忘录）
        LambdaQueryWrapper<Memo> highPriorityWrapper = new LambdaQueryWrapper<>();
        highPriorityWrapper.eq(Memo::getUserId, userId)
                .eq(Memo::getIsCompleted, false)
                .eq(Memo::getPriority, PriorityEnum.HIGH.getValue());
        long highPriority = memoMapper.selectCount(highPriorityWrapper);

        // 5. 逾期数（截止日期已过且未完成的备忘录）
        LambdaQueryWrapper<Memo> overdueWrapper = new LambdaQueryWrapper<>();
        overdueWrapper.eq(Memo::getUserId, userId)
                .eq(Memo::getIsCompleted, false)
                .lt(Memo::getDueDate, now)
                .isNotNull(Memo::getDueDate);
        long overdue = memoMapper.selectCount(overdueWrapper);

        return DailySummaryResponse.builder()
                .todayTotal((int) todayTotal)
                .completed((int) completed)
                .uncompleted((int) uncompleted)
                .highPriority((int) highPriority)
                .overdue((int) overdue)
                .summaryDate(today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                .build();
    }

    /**
     * 发送备忘录创建通知
     */
    private void sendMemoCreatedNotify(Memo memo) {
        try {
            Long tenantId = TenantContext.getTenantId();
            if (tenantId != null) {
                MemoDueMessage message = new MemoDueMessage();
                message.setTenantId(tenantId);
                message.setUserId(memo.getUserId());
                message.setMemoId(memo.getId());
                message.setDueDate(memo.getDueDate());
                message.setActionType("created");
                memoNotifyProducer.sendMemoDueMessage(message);
                log.info("发送备忘录创建通知: memoId={}, userId={}", memo.getId(), memo.getUserId());
            }
        } catch (Exception e) {
            // 异步通知失败不影响主业务流程
            log.warn("发送备忘录创建通知失败: memoId={}, error={}", memo.getId(), e.getMessage());
        }
    }

    /**
     * 发送备忘录更新通知
     */
    private void sendMemoUpdatedNotify(Memo memo) {
        try {
            Long tenantId = TenantContext.getTenantId();
            if (tenantId != null) {
                MemoDueMessage message = new MemoDueMessage();
                message.setTenantId(tenantId);
                message.setUserId(memo.getUserId());
                message.setMemoId(memo.getId());
                message.setDueDate(memo.getDueDate());
                message.setActionType("updated");
                memoNotifyProducer.sendMemoDueMessage(message);
                log.info("发送备忘录更新通知: memoId={}, userId={}", memo.getId(), memo.getUserId());
            }
        } catch (Exception e) {
            // 异步通知失败不影响主业务流程
            log.warn("发送备忘录更新通知失败: memoId={}, error={}", memo.getId(), e.getMessage());
        }
    }
}