package com.gzeic.memosystem.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gzeic.memosystem.tenant.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 日志切面
 * 用于记录所有Controller层请求的详细日志信息
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {
    private final ObjectMapper objectMapper;

    /**
     * 定义切点，匹配所有Controller层的方法
     */
    @Pointcut("execution(* com.gzeic.memosystem.controller..*.*(..))")
    public void controllerPointcut() {
    }

    /**
     * 环绕通知，记录请求的详细信息
     */
    @Around("controllerPointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String url = request.getRequestURI();
        String httpMethod = request.getMethod();
        String clientIp = request.getRemoteAddr();
        Long tenantId = TenantContext.getTenantId();
        // 记录请求开始信息
        log.info("====请求开始====");
        log.info("请求URL: {} {}", httpMethod, url);
        log.info("请求方法: {} {}", className, methodName);
        log.info("客户端IP: {}", clientIp);
        log.info("租户ID: {}", tenantId);
        log.info("请求头: {}", request.getHeaderNames());

        // 记录请求参数
        Object[] args = joinPoint.getArgs();
        if (args.length > 0) {
            try {
                String requestParams = objectMapper.writeValueAsString(args);
                log.info("请求参数: {}", requestParams);
            } catch (JsonProcessingException e) {
                log.info("请求参数无法序列化，使用toString: {}", args);
            }
        }

        Object result;

        // 执行目标方法
        try {
            result = joinPoint.proceed();
            stopWatch.stop();
            // 记录响应结果
            try {
                String responseResult = objectMapper.writeValueAsString(result);
                log.info("响应结果: {}", responseResult);
            } catch (Exception e) {
                log.info("响应结果无法序列化，使用toString: {}", result);
            }

            log.info("请求耗时: {}ms", stopWatch.getTotalTimeMillis());
            log.info("请求状态: 成功");
            log.info("====请求结束====\n");
        } catch (Throwable e) {
            stopWatch.stop();
            log.error("请求状态：失败");
            log.error("请求耗时: {}ms", stopWatch.getTotalTimeMillis());
            log.error("异常类型: {}", e.getClass().getName());
            log.error("异常信息: {}", e.getMessage());
            log.error("异常堆栈: ", e);
            log.info("====请求结束====\n");
            throw e;
        }
        return result;
    }
}
