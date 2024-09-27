package com.fisk.dataservice.dto.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class BusinessApiDTO {


    /**
     * 主键
     */
    @ApiModelProperty(value = "主键")
    public long id;

    /**
     * 应用名称
     */
    @ApiModelProperty(value = "应用名称")
    public String appName;

    public List<ApiListDTO> apiList;

}
