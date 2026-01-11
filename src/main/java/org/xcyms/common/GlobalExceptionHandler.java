package org.xcyms.common;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author liu-xu
 * @date 2026年01月11日 11:46
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 处理未登录异常
    @ExceptionHandler(NotLoginException.class)
    public ApiResult<?> handleNotLoginException(NotLoginException nle) {
        return ApiResult.error("未登录或会话已过期，请重新登录");
    }

    // 处理权限不足异常
    @ExceptionHandler(NotPermissionException.class)
    public ApiResult<?> handleNotPermissionException(NotPermissionException npe) {
        return ApiResult.error("您没有访问该功能的权限");
    }

    // 处理普通业务异常
    @ExceptionHandler(Exception.class)
    public ApiResult<?> handleException(Exception e) {
        log.error("全局异常捕获：" + e.getMessage(), e);
        return ApiResult.error(e.getMessage() != null ? e.getMessage() : "系统繁忙，请稍后再试");
    }
}
