package com.zpero.common.exception;


import com.zpero.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
/**
 *          全局异常处理器, 业务异常由后端定义, 全局异常由 handleException 处理,统一异常格式
 *
 *              java的异常分两类: 运行时异常 和  编译时异常
 *              BusinessException 继承 RuntimeException , 运行时异常, 可以在 try catch 中捕获 无需 throws 声明
 *
 *
 *
 * */

@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public Result handleBusinessException(BusinessException e) {

        return Result.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleAuthorizationDeniedException(AuthorizationDeniedException e) {
        return Result.fail(403, "权限不足");
    }

    @ExceptionHandler
    public Result handleException(Exception e) {
        log.error("全局异常处理: ", e);
        return Result.fail(500, "服务器内部错误: " + e.getMessage());
    }
}
