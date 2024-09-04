package com.fisk.dataaccess.dto.access;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class OdsFieldQueryDTO {


    /**
     * 表id  实时/非实时
     */
    @ApiModelProperty(value = "表id ")
    public String tblId;

    /**
     * 表类型   TableBusinessTypeEnum   该参数都需要传
     * PHYSICAL_TABLE(10,"physical_table"),//数接物理表
     * ACCESS_API(11,"standard_database"),//数接api
     * DORIS_CATALOG_TABLE(12,"doris_catalog_table"),//doris外部目录表
     */
    @ApiModelProperty(value = "表类型")
    public Integer tblType;

    /**
     * 外部目录名称   doris外部目录所需参数
     */
    @ApiModelProperty(value = "外部目录名称")
    public String catalogName;

    /**
     * 数据库id   该参数都需要传
     */
    @ApiModelProperty(value = "数据库id ")
    public Integer dbId;

    /**
     * 表所在数据库名称   doris外部目录所需参数
     */
    @ApiModelProperty(value = "表所在数据库名称")
    public String dbName;

    /**
     * 表名称   doris外部目录所需参数
     */
    @ApiModelProperty(value = "表名称")
    public String tblName;

    /**
     * 发布状态 0：未发布 1：已发布，只有表节点才有发布状态;   该参数都需要传
     */
    @ApiModelProperty(value = "发布状态 0：未发布 1：已发布，只有表节点才有发布状态")
    public String publishState;

}
