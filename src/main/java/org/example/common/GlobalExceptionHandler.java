package org.example.common;

import org.springframework.security.access.AccessDeniedException;

import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> handleBiz(BizException e) {
        return ApiResponse.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler({ MethodArgumentNotValidException.class, BindException.class,
            ConstraintViolationException.class })
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> handleValidate(Exception e) {
        return ApiResponse.fail(40001, "参数校验失败");
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> handleAccessDenied(AccessDeniedException e) {
        return ApiResponse.fail(403, "不允许访问: 权限不足");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> handleOther(Exception e) {
        e.printStackTrace();
        return ApiResponse.fail(500, "系统异常");
    }
}
