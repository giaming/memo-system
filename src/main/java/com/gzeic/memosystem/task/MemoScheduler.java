package com.gzeic.memosystem.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gzeic.memosystem.config.TenantProperties;
import com.gzeic.memosystem.entity.Memo;
import com.gzeic.memosystem.mq.MemoDueMessage;
import com.gzeic.memosystem.mq.MemoNotifyProducer;
import com.gzeic.memosystem.mapper.MemoMapper;
import com.gzeic.memosystem.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 备忘录相关定时任务（Project 07）
 *
 * 重要提示（多租户）：
 * - 定时任务不是 HTTP 请求，不会经过 TenantHeaderFilter/JWT 过滤器
 * - 因此必须在任务代码里“手动设置 TenantContext”，并且执行完要清理
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MemoScheduler {

    private final TenantProperties tenantProperties;
    private final MemoMapper memoMapper;
    private final MemoNotifyProducer memoNotifyProducer;

    /**
     * 每日待办汇总（教学版：按租户统计未完成数量）
     *
     * cron 说明：
     * - Spring 的 cron 有 6 位（秒 分 时 日 月 周）
     * - 这里是每天 08:00:00 执行一次
     */
    @Scheduled(cron = "0 0 8 * * ?")
    public void dailyTodoSummary() {
        List<Long> tenants = tenantProperties.getTenants();
        if (tenants == null || tenants.isEmpty()) {
            return;
        }

        for (Long tenantId : tenants) {
            try {
                TenantContext.setTenantId(tenantId);

                LambdaQueryWrapper<Memo> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(Memo::getIsCompleted, false);
                Long count = memoMapper.selectCount(wrapper);

                log.info("每日待办汇总 tenantId={}, 未完成备忘录数量={}", tenantId, count);
            } finally {
                TenantContext.clear();
            }
        }
    }

    /**
     * 到期检查：扫描到期且未提醒的备忘录，并投递 MQ 消息
     *
     * 说明（教学版策略）：
     * - 每分钟扫描一次（便于课堂演示）
     * - 通过字段 is_notified 控制幂等，避免重复发送
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void checkDueMemosAndNotify() {
        List<Long> tenants = tenantProperties.getTenants();
        if (tenants == null || tenants.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        for (Long tenantId : tenants) {
            try {
                TenantContext.setTenantId(tenantId);

                LambdaQueryWrapper<Memo> wrapper = new LambdaQueryWrapper<>();
                wrapper.isNotNull(Memo::getDueDate)
                        .le(Memo::getDueDate, now)
                        .eq(Memo::getIsCompleted, false)
                        .eq(Memo::getIsNotified, false);

                List<Memo> dueMemos = memoMapper.selectList(wrapper);
                if (dueMemos.isEmpty()) {
                    continue;
                }

                for (Memo memo : dueMemos) {
                    MemoDueMessage message = new MemoDueMessage();
                    message.setTenantId(tenantId);
                    message.setUserId(memo.getUserId());
                    message.setMemoId(memo.getId());
                    message.setDueDate(memo.getDueDate());

                    memoNotifyProducer.sendMemoDueMessage(message);

                    memo.setIsNotified(true);
                    memoMapper.updateById(memo);

                    log.info("已投递到期提醒 tenantId={}, memoId={}, userId={}", tenantId, memo.getId(), memo.getUserId());
                }
            } finally {
                TenantContext.clear();
            }
        }
    }
}

