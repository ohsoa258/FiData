package com.fisk.dataaccess.dto.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author Lock
 * @version 1.3
 * @description
 * @date 2022/1/17 15:44
 */
@Data
public class GenerateApiDTO {

//    @ApiModelProperty(value = "总条数", required = true)
//    public Long total;
//    @ApiModelProperty(value = "批次号", required = true)
//    public String batchNumber;
    @ApiModelProperty(value = "推送表标识", required = true)
    public Long tableIdentity;
    @ApiModelProperty(value = "字段集合", required = true)
    public List<String> fieldList;
    @ApiModelProperty(value = "子级", required = true)
    public List<GenerateApiDTO> data;
}
