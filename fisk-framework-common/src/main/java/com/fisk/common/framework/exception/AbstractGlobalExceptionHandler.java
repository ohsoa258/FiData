package com.fisk.common.framework.exception;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.mdc.MDCHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 统一异常处理
 *
 * @author gy
 */
@RestControllerAdvice
@ResponseBody
@Slf4j
public abstract class AbstractGlobalExceptionHandler {

    /**
     * 模型验证报错
     *
     * @param ex 异常信息
     * @return 请求响应对象
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResultEntity<Object> handle1(MethodArgumentNotValidException ex) {
        ResultEntity<Object> res;
        BeanPropertyBindingResult bindingResult = (BeanPropertyBindingResult) ex.getBindingResult();
        if (bindingResult.hasErrors()) {
            StringBuilder str = new StringBuilder();
            for (FieldError allError : bindingResult.getFieldErrors()) {
                str.append("字段：【").append(allError.getField()).append("】，").append("错误信息：【").append(allError.getDefaultMessage()).append("】。");
            }
            log.error(str.toString());
            res = ResultEntityBuild.build(ResultEnum.SAVE_VERIFY_ERROR, str.toString());
        } else {
            res = ResultEntityBuild.build(ResultEnum.ERROR, ex.toString());
        }
        res.traceId = MDCHelper.getTraceId();
        MDCHelper.clear();
        return res;
    }

    /**
     * 系统自定义异常
     *
     * @param ex 异常信息
     * @return 请求响应对象
     */
    @ExceptionHandler(value = FkException.class)
    public ResultEntity<Object> handle1(FkException ex) {
        String traceId = MDCHelper.getTraceId();
        log.error("全局异常拦截：" + ex.toString());
        MDCHelper.clear();
        if (StringUtils.isNotEmpty(ex.getErrorMsg())) {
            ResultEntity<Object> res = ResultEntityBuild.build(ex.getResultEnum(), ex.getErrorMsg());
            res.traceId = traceId;
            return res;
        }
        ResultEntity<Object> res = ResultEntityBuild.build(ex.getResultEnum());
        res.traceId = traceId;
        return res;
    }

    /**
     * 代码未检查到的报错
     *
     * @param ex 异常信息
     * @return 请求响应对象
     */
    @ExceptionHandler(value = Exception.class)
    public ResultEntity<Object> handle1(Exception ex) {
        String traceId = MDCHelper.getTraceId();
        log.error("全局异常拦截：" + ex.toString());
        MDCHelper.clear();
        ResultEntity<Object> res = ResultEntityBuild.build(ResultEnum.ERROR, ex.getMessage());
        res.traceId = traceId;
        return res;
    }

}
