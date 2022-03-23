package com.fisk.common.response;

import lombok.Data;

/**
 * 请求结果对象
 * @author gy
 */
@Data
public class ResultEntity<T> {
    public int code;
    public String msg;
    public T data;
}