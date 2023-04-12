package com.fisk.datagovernance.vo.datasecurity.intelligentdiscovery;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class IntelligentDiscovery_RuleExtInfo_SchemaVO {
    /**
     * 数据源ip
     */
    @ApiModelProperty(value = "数据源ip")
    public String ip;

    /**
     * 数据源库名称
     */
    @ApiModelProperty(value = "数据源库名称")
    public String dataBaseName;

    /**
     * 模式
     */
    @ApiModelProperty(value = "模式")
    public String schema;

    /**
     * 模式下的表
     */
    @ApiModelProperty(value = "模式下的表")
    public List<String> tableNameList;
}
