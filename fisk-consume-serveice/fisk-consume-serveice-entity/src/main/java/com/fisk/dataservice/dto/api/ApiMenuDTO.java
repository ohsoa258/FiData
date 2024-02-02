package com.fisk.dataservice.dto.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2024-01-26
 * @Description:
 */
@Data
public class ApiMenuDTO {
    @ApiModelProperty(value = "")
    private Integer id;

    @ApiModelProperty(value = "父id")
    private Integer pid;

    @ApiModelProperty(value = "标签名称")
    private String name;

    @ApiModelProperty(value = "类型:1:目录 2:数据")
    private Integer type;

    @ApiModelProperty(value = "服务类型：1.本地服务 2.代理服务")
    private Integer serverType;

    @ApiModelProperty(value = "排序")
    private Integer sort;
}
