package com.fisk.dataaccess.dto.api;

import io.swagger.annotations.ApiModelProperty;
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

    @ApiModelProperty(value = "总数")
    public Integer total;
    @ApiModelProperty(value = "批号")
    public String batchNumber;
    @ApiModelProperty(value = "表ID对象")
    public Integer tableIdentity;
    @ApiModelProperty(value = "哈希图")
    public HashMap<String, Object> hashMap;

    @ApiModelProperty(value = "数据")
    public Object data;
}