package com.fisk.apiservice.vo.dataservice;

import lombok.Data;

@Data
public class TokenResponseVO {
    public int code;

    public String msg;

    public String data;
}
