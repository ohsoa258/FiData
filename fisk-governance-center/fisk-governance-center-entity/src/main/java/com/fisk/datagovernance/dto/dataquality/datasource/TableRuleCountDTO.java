package com.fisk.datagovernance.dto.dataquality.datasource;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 表规则数据DTO
 * @date 2022/11/30 15:39
 */
@Data
public class TableRuleCountDTO {
    /**
     * 数据源id
     */
    @ApiModelProperty(value = "数据源id")
    public int sourceId;

    /**
     * 表标识
     */
    @ApiModelProperty(value = "表标识")
    public String tableUnique;

    /**
     * 表规则数量
     */
    @ApiModelProperty(value = "表规则数量")
    public int tableRuleCount;

    /**
     * 表业务类型 1：事实表、2：维度表、3、指标表   4、宽表
     */
    @ApiModelProperty(value = "业务类型，表和视图维度设置")
    public int tableBusinessType;

    /**
     * 1 TABLE / 2 VIEW
     */
    @ApiModelProperty(value = "1 TABLE / 2 VIEW")
    public int tableType;

    /**
     * 表规则类型 校验规则/清洗规则/回收规则
     */
    @ApiModelProperty(value = "表规则类型 校验规则/清洗规则/回收规则")
    public String tableRuleType;
}
