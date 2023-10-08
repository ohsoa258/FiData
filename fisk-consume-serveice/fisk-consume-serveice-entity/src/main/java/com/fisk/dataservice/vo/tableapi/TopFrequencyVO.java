package com.fisk.dataservice.vo.tableapi;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2023-10-07
 * @Description:
 */
@Data
public class TopFrequencyVO {
    @ApiModelProperty(value = "api名称")
    private String apiName;
    @ApiModelProperty(value = "频率")
    private int frequency;
}
