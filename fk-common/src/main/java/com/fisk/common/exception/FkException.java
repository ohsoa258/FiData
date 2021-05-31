package com.fisk.common.exception;

import com.fisk.common.response.ResultEnum;
import lombok.Getter;


@Getter
public class FkException extends RuntimeException {
    /**
     * 异常状态码信息
     */
    private int status;
    private ResultEnum resultEnum;
    private String errorMsg;

    public FkException(int status) {
        this.status = status;
    }

    public FkException(int status, String message) {
        super(message);
        this.status = status;
    }

    public FkException(int status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public FkException(int status, Throwable cause) {
        super(cause);
        this.status = status;
    }

    public FkException(ResultEnum resultEnum) {
        super(resultEnum.getMsg());
        this.resultEnum = resultEnum;
        this.status = resultEnum.getCode();
    }

    public FkException(ResultEnum resultEnum, String msg) {
        super(resultEnum.getMsg());
        this.resultEnum = resultEnum;
        this.errorMsg = msg;
    }
}