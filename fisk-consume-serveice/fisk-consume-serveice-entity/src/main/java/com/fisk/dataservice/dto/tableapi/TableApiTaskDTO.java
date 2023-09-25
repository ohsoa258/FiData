package com.fisk.dataservice.dto.tableapi;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Map;

/**
 * @Author: wangjian
 * @Date: 2023-09-13
 * @Description:
 */
@Data
public class TableApiTaskDTO {
    @ApiModelProperty(value = "pipelTaskTraceId")
    public String pipelTaskTraceId;
    @ApiModelProperty(value = "apiId")
    public Long apiId;
    @ApiModelProperty(value = "type")
    public Integer tableType;
    @ApiModelProperty(value = "task日志信息")
    private Map<Integer,Object> msg;
}
