package com.gzeic.memosystem.exception;

/**
 * 未授权异常（通常表示：没有登录 / Token 无效 / Token 过期）
 *
 * 设计目的：
 * - 让“鉴权失败”走统一的异常链路，最终由全局异常处理器转换为统一的 Result 返回结构
 * - 避免在拦截器/过滤器里手写 response 输出 JSON，导致返回格式不统一
 */
public class UnauthorizedException extends RuntimeException {

    /**
     * 构造一个未授权异常
     *
     * @param message 给前端/调用者看的提示信息（不要包含敏感信息）
     */
    public UnauthorizedException(String message) {
        super(message);
    }
}

