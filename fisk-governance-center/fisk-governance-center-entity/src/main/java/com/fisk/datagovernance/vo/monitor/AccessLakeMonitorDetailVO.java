package com.fisk.datagovernance.vo.monitor;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2023-12-21
 * @Description:
 */
@Data
public class AccessLakeMonitorDetailVO {
    @ApiModelProperty(value = "来源类型")
    private String sourceDriverName;
    @ApiModelProperty(value = "来源库")
    private String sourceDbName;
    @ApiModelProperty(value = "来源表")
    private String sourceTableName;
    @ApiModelProperty(value = "源表数据行")
    private Integer sourceRows;
    @ApiModelProperty(value = "目标类型")
    private String targetDriverName;
    @ApiModelProperty(value = "目标库")
    private String targetDbName;
    @ApiModelProperty(value = "目标表")
    private String targetTableName;
    @ApiModelProperty(value = "目标数据行")
    private Integer targetRows;
}
