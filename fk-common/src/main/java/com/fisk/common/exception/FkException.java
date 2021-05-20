package com.fisk.common.exception;

import lombok.Getter;


@Getter
public class FkException extends RuntimeException {
    /**
     * 异常状态码信息
     */
    private int status;

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
}