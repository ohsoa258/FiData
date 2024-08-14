package com.fisk.datamanagement.dto.DataSet;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2024-01-30
 * @Description:
 */
@Data
public class CodeSetDTO {
    @ApiModelProperty(value = "id")
    public Integer id;
    @ApiModelProperty(value = "集合Id")
    public Integer collectionId;
    @ApiModelProperty(value = "编号")
    public String code;
    @ApiModelProperty(value = "名称")
    public String name;
    @ApiModelProperty(value = "描述")
    public String description;
}
