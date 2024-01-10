package com.fisk.datagovernance.dto.dataops;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2024-01-03
 * @Description:
 */
@Data
public class DataObsSqlDTO {
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "tab名称")
    private String tabName;

    @ApiModelProperty(value = "querySql")
    private String querySql;

    @ApiModelProperty(value = "数据库id")
    private String dbId;
}
