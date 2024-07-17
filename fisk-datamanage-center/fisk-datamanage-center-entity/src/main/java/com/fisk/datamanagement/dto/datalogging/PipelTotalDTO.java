package com.fisk.datamanagement.dto.datalogging;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2024-07-16
 * @Description:
 */
@Data
public class PipelTotalDTO {
    @ApiModelProperty(value = "总条数")
    private Integer total;
    @ApiModelProperty(value = "成功条数")
    private Integer successTotal;
    @ApiModelProperty(value = "失败条数")
    private Integer failTotal;
}
