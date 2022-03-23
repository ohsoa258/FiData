package com.fisk.dataaccess.dto.api;

import lombok.Data;

import java.util.HashMap;

/**
 * @author Lock
 * @version 1.3
 * @description
 * @date 2022/1/18 13:59
 */
@Data
public class ParseJsonObjectDTO {

    public Integer total;
    public String batchNumber;
    public Integer tableIdentity;
    public HashMap<String, Object> hashMap;
    public Object data;
}