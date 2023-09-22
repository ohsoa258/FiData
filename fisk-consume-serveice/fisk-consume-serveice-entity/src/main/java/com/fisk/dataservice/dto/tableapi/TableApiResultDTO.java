package com.fisk.dataservice.dto.tableapi;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2023-09-11
 * @Description:
 */
@Data
public class TableApiResultDTO {
    @ApiModelProperty(value = "id")
    private int id;

    @ApiModelProperty(value = "appId")
    private int appId;

    @ApiModelProperty(value = "pid")
    private int pid;

    @ApiModelProperty(value = "字段名称")
    private String name;

    @ApiModelProperty(value = "1:选中0:不选")
    private int selected;

    @ApiModelProperty(value = "id",hidden = true)
    private int copyId;

    @ApiModelProperty(value = "pid",hidden = true)
    private int copyPid;
}
