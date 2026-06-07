package com.gzeic.memosystem.config;

import com.gzeic.memosystem.exception.BusinessException;
import com.gzeic.memosystem.exception.UnauthorizedException;
import com.gzeic.memosystem.util.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    /**
     * 处理“未登录/未授权”等鉴权异常
     */
    @ExceptionHandler(UnauthorizedException.class)
    @ResponseBody
    public Result<?> handleUnauthorizedException(UnauthorizedException e) {
        log.warn("鉴权失败: {}", e.getMessage());
        return Result.unauthorized(e.getMessage());
    }

    /**
     * 处理业务异常（可预期的错误）
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseBody
    public Result<?> handleBusinessException(BusinessException e) {
        log.warn("业务异常(code={}): {}", e.getCode(), e.getMessage());
        return Result.of(e.getCode(), e.getMessage(), null);
    }

    /**
     * 处理请求体参数校验失败（@Valid + @RequestBody）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public Result<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getAllErrors().isEmpty()
                ? "参数校验失败"
                : e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        log.warn("参数校验失败: {}", message);
        return Result.fail(message);
    }

    /**
     * 处理表单参数/路径参数校验失败（@Valid 作用在普通对象上时可能出现）
     */
    @ExceptionHandler(BindException.class)
    @ResponseBody
    public Result<?> handleBindException(BindException e) {
        String message = e.getBindingResult().getAllErrors().isEmpty()
                ? "参数绑定失败"
                : e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        log.warn("参数绑定失败: {}", message);
        return Result.fail(message);
    }

    /**
     * 处理运行时异常
     * @param e
     * @return
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseBody
    public Result<?> handleRuntimeException(RuntimeException e) {
        log.error("运行时异常: {}", e.getMessage(), e);
        return Result.error(e.getMessage());
    }

    /**
     * 处理所有异常
     * @param e
     * @return
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result<?> handleException(Exception e) {
        log.error("系统异常: {}", e.getMessage(), e);
        return Result.error("系统内部错误，请稍后重试");
    }
}
