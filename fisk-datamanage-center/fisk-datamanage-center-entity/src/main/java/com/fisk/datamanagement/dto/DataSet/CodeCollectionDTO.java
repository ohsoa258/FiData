package com.fisk.datamanagement.dto.DataSet;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2024-01-30
 * @Description:
 */
@Data
public class CodeCollectionDTO {
    @ApiModelProperty(value = "id")
    public Integer id;
    @ApiModelProperty(value = "集合名称")
    public String collectionName;
    @ApiModelProperty(value = "描述")
    public String description;
}
