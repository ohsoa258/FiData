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
     * 描述
     */
    @ApiModelProperty(value = "描述")
    public String describe;

    /**
     * 状态
     */
    @ApiModelProperty(value = "状态")
    public String stateName;

    /**
     * 数据源类型
     */
    @ApiModelProperty(value = "数据源类型")
    public String sourceTypeName;

    /**
     * 库IP
     */
    @ApiModelProperty(value = "库IP")
    public String ip;

    /**
     * 库名称
     */
    @ApiModelProperty(value = "库名称")
    public String dbName;

    /**
     * 表名称，携带架构名
     */
    @ApiModelProperty(value = "表名称，携带架构名")
    public String tableName;
}
