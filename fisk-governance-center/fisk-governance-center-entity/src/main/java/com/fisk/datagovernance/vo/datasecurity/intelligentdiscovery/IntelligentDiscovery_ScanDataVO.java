package com.fisk.datagovernance.vo.datasecurity.intelligentdiscovery;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class IntelligentDiscovery_ScanDataVO {
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
     * 扫描的字段类型
     */
    @ApiModelProperty(value = "扫描的字段类型")
    public String fieldType;

    /**
     * 扫描的字段类型长度
     */
    @ApiModelProperty(value = "扫描的字段类型长度")
    public String fieldLength;

    /**
     * 扫描的字段注释
     */
    @ApiModelProperty(value = "扫描的字段注释")
    public String fieldComment;

    /**
     * 扫描的字段是否是主键
     */
    @ApiModelProperty(value = "扫描的字段是否是主键")
    public String fieldIsPrimaryKey;

    /**
     * 扫描的字段默认值
     */
    @ApiModelProperty(value = "扫描的字段默认值")
    public String fieldDefaultValue;

    /**
     * 扫描的字段是否允许为空
     */
    @ApiModelProperty(value = "扫描的字段是否允许为空")
    public String fieldIsAllowNull;

    /**
     * 字段状态
     * 1：字段存在白名单中，点击移出白名单
     * 2：字段不存在白名单中，点击移入白名单
     */
    @ApiModelProperty(value = "字段状态：1 字段存在白名单中，点击移出白名单 2 字段不存在白名单中，点击移入白名单")
    public int fieldState;
}
