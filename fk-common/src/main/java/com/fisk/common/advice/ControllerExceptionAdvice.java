package com.fisk.common.advice;

import com.fisk.common.exception.FkException;
import com.fisk.common.exception.FkException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice // 默认情况下,会拦截所有加了@Controller的类
@Slf4j
public class ControllerExceptionAdvice {

    /**
     * 统一异常处理方法，@ExceptionHandler(RuntimeException.class)声明这个方法处理RuntimeException这样的异常
     * @param e 捕获到的异常
     * @return 返回给页面的状态码和信息
     */
    @ExceptionHandler(FkException.class)
    public ResponseEntity<String> handleLyException(FkException e) {
        return ResponseEntity.status(e.getStatus()).body(e.getMessage());
    }
}