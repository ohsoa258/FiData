package com.fisk.mdm.vo.process;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2023-04-10
 * @Description:
 */
@Data
public class PersonVO {
    @ApiModelProperty(value = "审核人")
    private String approval;
    @ApiModelProperty(value = "描述")
    private String description;
    @ApiModelProperty(value = "状态")
    private String state;
}
