package com.fisk.datagovernance.vo.monitor;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2024-10-11
 * @Description:
 */
@Data
public class ServerMonitorTypeVO {
    @ApiModelProperty(value = "id")
    private Integer id;
    @ApiModelProperty(value = "自定义类型")
    private String serverType;
}
