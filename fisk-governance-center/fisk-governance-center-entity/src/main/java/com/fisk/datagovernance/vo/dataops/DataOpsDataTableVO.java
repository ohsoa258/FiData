package com.fisk.datagovernance.vo.dataops;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 数据运维表、字段
 * @date 2022/4/25 15:06
 */
@Data
public class DataOpsDataTableVO {
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

    /**
     * 表字段
     */
    @ApiModelProperty(value = "表字段")
    public List<DataOpsTableFieldVO> children;
}
