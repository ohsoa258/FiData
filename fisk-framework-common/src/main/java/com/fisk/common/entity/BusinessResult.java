package com.fisk.common.entity;

import lombok.AllArgsConstructor;

/**
 * 业务方法返回结果
 *
 * @author gy
 */
@AllArgsConstructor(staticName = "of")
public class BusinessResult<T> {
    public boolean success;
    public String msg;
    public T data;

    public BusinessResult(boolean success, String msg) {
        this.success = success;
        this.msg = msg;
    }
}
