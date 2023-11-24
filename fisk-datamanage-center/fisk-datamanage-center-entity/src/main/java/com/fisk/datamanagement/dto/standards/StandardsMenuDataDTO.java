package com.fisk.datamanagement.dto.standards;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2023-11-23
 * @Description:
 */
@Data
public class StandardsMenuDataDTO {
    @ApiModelProperty(value = "id")
    private Integer id;
    @ApiModelProperty(value = "标签名称")
    private String MenuName;
    @ApiModelProperty(value = "详情名称")
    private StandardsDTO standard;
}
