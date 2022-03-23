package com.fisk.common.exception;

import com.fisk.common.response.ResultEnum;
import lombok.Getter;

/**
 * @author Lock
 */
@Getter
public class FkException extends RuntimeException {
    /**
     * 异常状态码信息
     */
    private ResultEnum resultEnum;
    private String errorMsg;

    public FkException(ResultEnum resultEnum) {
        super(resultEnum.getMsg());
        this.resultEnum = resultEnum;
    }

    public FkException(ResultEnum resultEnum, String msg) {
        super(resultEnum.getMsg());
        this.resultEnum = resultEnum;
        this.errorMsg = msg;
    }

    public FkException(ResultEnum resultEnum, Throwable cause) {
        super(resultEnum.getMsg(), cause);
        this.resultEnum = resultEnum;
    }

    public FkException(ResultEnum resultEnum, String message, Throwable cause) {
        super(message, cause);
        this.resultEnum = resultEnum;
    }
}