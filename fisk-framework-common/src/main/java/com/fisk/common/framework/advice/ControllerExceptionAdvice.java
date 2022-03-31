package com.fisk.common.framework.advice;

import com.fisk.common.framework.exception.FkException;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * @author gy
 */
@ControllerAdvice
@Slf4j
public class ControllerExceptionAdvice {

    /**
     * 统一异常处理方法，@ExceptionHandler(RuntimeException.class)声明这个方法处理RuntimeException这样的异常
     * @param ex 捕获到的异常
     * @return 返回给页面的状态码和信息
     */
    @ExceptionHandler(FkException.class)
    public ResultEntity<Object> handleLyException(FkException ex) {
        if (StringUtils.isNotEmpty(ex.getErrorMsg())) {
            return ResultEntityBuild.build(ex.getResultEnum(), ex.getErrorMsg());
        }
        return ResultEntityBuild.build(ex.getResultEnum());
    }
}