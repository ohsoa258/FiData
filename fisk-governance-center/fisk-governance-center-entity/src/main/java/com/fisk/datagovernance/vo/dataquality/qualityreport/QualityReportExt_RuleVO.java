package com.fisk.datagovernance.vo.dataquality.qualityreport;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 质量报告扩展信息VO
 * @date 2022/12/1 10:46
 */
@Data
public class QualityReportExt_RuleVO {
    /**
     * id
     */
    @ApiModelProperty(value = "标识")
    public Long id;

    /**
     * 名称
     */
    @ApiModelProperty(value = "名称")
    public String name;

    /**
     * 类型名称
     */
    @ApiModelProperty(value = "类型名称")
    public String typeName;

    /**
     * 状态名称
     */
    @ApiModelProperty(value = "状态名称")
    public String stateName;

    /**
     * 顺序
     */
    @ApiModelProperty(value = "顺序")
    public int sort;

    /**
     * 数据源类型名称
     */
    @ApiModelProperty(value = "数据源类型名称")
    public String sourceTypeName;

    /**
     * IP
     */
    @ApiModelProperty(value = "IP")
    public String ip;

    /**
     * 库名称
     */
    @ApiModelProperty(value = "库名称")
    public String dbName;

    /**
     * 表名称
     */
    @ApiModelProperty(value = "表名称")
    public String tableName;

    /**
     * 表别名
     */
    @ApiModelProperty(value = "表别名")
    public String tableAliasName;

    /**
     * 表类型
     */
    @ApiModelProperty(value = "表类型")
    public String tableTypeName;
}
