package com.fisk.dataservice.dto.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2024-09-25
 * @Description:
 */
@Data
public class ApiListDTO {

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    public LocalDateTime createTime;

    /**
     * apiId
     */
    @ApiModelProperty(value = "apiId")
    public int apiId;
    /**
     * api名称
     */
    @ApiModelProperty(value = "api名称")
    public String apiName;
    /**
     * 维度字段列表
     */
    @ApiModelProperty(value = "维度字段列表")
    public List<ApiAttributeDTO> attributeList;
}
