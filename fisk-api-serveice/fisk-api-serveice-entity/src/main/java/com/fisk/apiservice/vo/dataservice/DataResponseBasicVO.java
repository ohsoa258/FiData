package com.fisk.apiservice.vo.dataservice;

import lombok.Data;

@Data
public class DataResponseBasicVO {
    public int code;

    public String msg;

    public DataResponseVO data;
}
