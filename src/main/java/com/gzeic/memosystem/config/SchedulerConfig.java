package com.gzeic.memosystem.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 定时任务开关配置
 *
 * 说明：
 * - 加上 @EnableScheduling 后，Spring 才会识别并执行 @Scheduled 标注的方法
 * - Project 07 会用它实现“每日汇总”和“到期检查并投递 MQ”
 */
@Configuration
@EnableScheduling
public class SchedulerConfig {
}

