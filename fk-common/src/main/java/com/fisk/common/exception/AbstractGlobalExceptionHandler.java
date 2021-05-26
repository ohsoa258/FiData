package com.fisk.common.exception;

import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Set;

/**
 * 统一异常处理
 *
 * @author gy
 */
@RestControllerAdvice
@ResponseBody
public abstract class AbstractGlobalExceptionHandler {

    //TODO: 缺少日志记录

    /**
     * 模型验证报错
     *
     * @param ex 异常信息
     * @return 请求响应对象
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResultEntity<Object> handle1(MethodArgumentNotValidException ex) {
        BeanPropertyBindingResult bindingResult = (BeanPropertyBindingResult) ex.getBindingResult();
        if (bindingResult.hasErrors()) {
            StringBuilder str = new StringBuilder();
            for (FieldError allError : bindingResult.getFieldErrors()) {
                str.append("字段：【").append(allError.getField()).append("】，").append("错误信息：【").append(allError.getDefaultMessage()).append("】。");
            }
            return ResultEntityBuild.build(ResultEnum.SAVE_VERIFY_ERROR, str.toString());
        } else {
            return ResultEntityBuild.build(ResultEnum.ERROR, ex.toString());
        }
    }

    /**
     * 代码未检查到的报错
     *
     * @param ex 异常信息
     * @return 请求响应对象
     */
    @ExceptionHandler(value = Exception.class)
    public ResultEntity<Object> handle1(Exception ex) {
        return ResultEntityBuild.build(ResultEnum.ERROR, ex.toString());
    }

}
