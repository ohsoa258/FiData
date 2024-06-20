package com.fisk.common.service.metadata.dto.metadata;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 * @date 2022-07-01 14:33
 */
@Data
public class MetaDataTableAttributeDTO extends MetaDataBaseAttributeDTO {
    @ApiModelProperty(value = "字段集合")
    public List<MetaDataColumnAttributeDTO> columnList;


    /**
     * 是否存在所属临时表 null true则存在 ，false不存在
     */
    public Boolean isExistStg;

    /**
     * 是否是数据接入CDC
     */
    public Boolean isCDC;

    /**
     * 是否关联存在业务分类 null true则存在 ，false不存在
     */
    public Boolean isExistClassification;

    /**
     *  应用名称（数据接入、数据建模、数据消费) 模型名(主数据)
     */
    public String AppName;

    /**
     *  应用ID（数据接入、数据建模、数据消费) 模型ID(主数据)
     */
    public Integer AppId;

    /**
     *  应用类型(数据接入)  0 1 2 实时 非实时 CDC
     */

    public Integer AppType;

    /**
     * 源表配置ID
     */
    public Integer tableConfigId;

    /**
     * 来源表到STG表 sql
     */
    public String sqlScript;

    /**
     * STG到目标表SQL
     */
    public String coverScript;

    /**
     *  数据源ID
     */
    public Integer dataSourceId;

    /**
     * 是否为公共维度
     */
    public Boolean isShareDim;

    /**
     * 是否将应用简称作为schema使用
     * 否：0  false
     * 是：1  true
     */
    public Boolean whetherSchema;

    /**
     * 当是数仓建模时  这个集合代表数仓建模事实表关联外键时关联的那些维度表
     */
    public List<String> dimQNames;

    /**
     * 当是数据接入 CDC类型应用时 该集合里面装载是cdc配置表的上有来源表名称
     */
    public List<String> cdcFromTableList;

}
