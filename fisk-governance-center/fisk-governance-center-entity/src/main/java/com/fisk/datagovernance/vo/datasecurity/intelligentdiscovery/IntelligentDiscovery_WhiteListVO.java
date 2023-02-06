package com.fisk.datagovernance.vo.datasecurity.intelligentdiscovery;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class IntelligentDiscovery_WhiteListVO {
    /**
     * 主键ID
     */
    @ApiModelProperty(value = "主键ID")
    public int id;

    /**
     * 扫描的数据库ip
     */
    @ApiModelProperty(value = "扫描的数据库ip")
    public String scanDatabaseIp;

    /**
     * 扫描的数据库名称
     */
    @ApiModelProperty(value = "扫描的数据库名称")
    public String scanDatabase;

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

    /**
     * 扫描的字段
     */
    @ApiModelProperty(value = "扫描的字段")
    public String scanField;

    /**
     * 有效性：1 生效中 2 已失效
     */
    @ApiModelProperty(value = "有效性：1 生效中 2 已失效")
    public int validity;
}
