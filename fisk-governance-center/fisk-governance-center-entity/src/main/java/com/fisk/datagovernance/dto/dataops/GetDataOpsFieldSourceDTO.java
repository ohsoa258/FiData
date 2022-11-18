package com.fisk.datagovernance.dto.dataops;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description TDDD
 * @date 2022/11/14 14:41
 */
@Data
public class GetDataOpsFieldSourceDTO {
    /**
     * 数据源id
     */
    @ApiModelProperty(value = "数据源id")
    public int datasourceId;

    /**
     * 表名称
     */
    @ApiModelProperty(value = "表名称")
    public String tableName;

    /**
     * 表架构名称
     */
    @ApiModelProperty(value = "表架构名称")
    public String tableFramework;

}
