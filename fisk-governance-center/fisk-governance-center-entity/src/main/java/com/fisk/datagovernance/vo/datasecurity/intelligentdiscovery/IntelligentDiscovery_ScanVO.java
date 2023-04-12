package com.fisk.datagovernance.vo.datasecurity.intelligentdiscovery;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class IntelligentDiscovery_ScanVO {
    /**
     * 主键ID
     */
    @ApiModelProperty(value = "主键ID")
    public int id;

    /**
     * tb_Intelligentdiscovery_rule表主键ID
     */
    @ApiModelProperty(value = "tb_Intelligentdiscovery_rule表主键ID")
    public int ruleId;

    /**
     * FiData数据源ID
     */
    @ApiModelProperty(value = "FiData数据源ID")
    public int datasourceId;

    /**
     * 扫描的数据库模式
     */
    @ApiModelProperty(value = "扫描的数据库模式")
    public String scanSchema;

    /**
     * 扫描的表
     */
    @ApiModelProperty(value = "扫描的表")
    public String scanTable;
}
