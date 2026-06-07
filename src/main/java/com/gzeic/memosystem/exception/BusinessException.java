package com.gzeic.memosystem.exception;

/**
 * 业务异常（用于“逻辑上可以预期”的错误）
 *
 * 典型场景：
 * - 参数不符合业务规则（例如：标题为空、截止时间早于当前时间）
 * - 数据不存在（例如：要修改的备忘录 ID 不存在）
 * - 权限不够（例如：用户试图操作不属于自己的备忘录）
 *
 * 为什么要有业务异常：
 * - 业务异常不是系统崩溃，不应该返回 500
 * - 我们希望返回统一结构 Result，并且 code 可控（通常是 400/403 等）
 */
public class BusinessException extends RuntimeException {

    /**
     * 业务错误码（建议与 HTTP 状态码含义保持一致，例如 400/403）
     */
    private final int code;

    /**
     * 构造业务异常
     *
     * @param code    业务错误码
     * @param message 给前端/调用者看的提示信息（不要包含敏感信息）
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}

