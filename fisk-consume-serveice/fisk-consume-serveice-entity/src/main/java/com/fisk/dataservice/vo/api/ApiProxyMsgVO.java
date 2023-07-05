package com.fisk.dataservice.vo.api;

import lombok.Data;

import java.io.Serializable;

/**
 * @author dick
 * @version 1.0
 * @description API代理消息
 * @date 2023/6/7 17:45
 */
@Data
public class ApiProxyMsgVO implements Serializable {
    public int code;
    public String msg;
}
