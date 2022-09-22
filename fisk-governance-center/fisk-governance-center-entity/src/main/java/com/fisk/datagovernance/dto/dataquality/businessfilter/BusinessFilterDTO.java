package com.fisk.datagovernance.dto.dataquality.businessfilter;

import com.fisk.datagovernance.enums.dataquality.RuleStateEnum;
import com.fisk.datagovernance.enums.dataquality.SourceTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 业务清洗DTO
 * @date 2022/3/24 13:48
 */
@Data
public class BusinessFilterDTO {
    /**
     * 模板id
     */
    @ApiModelProperty(value = "模板id")
    public int templateId;

    /**
     * 数据源表主键id
     */
    @ApiModelProperty(value = "数据源表主键id")
    public int datasourceId;

    /**
     * 数据源类型
     */
    @ApiModelProperty(value = "数据源类型")
    public SourceTypeEnum sourceTypeEnum;


    /**
     * 规则名称
     */
    @ApiModelProperty(value = "规则名称")
    public String ruleName;

    /**
     * 表名称/表Id
     */
    @ApiModelProperty(value = "表名称/表Id")
    public String tableUnique;

    /**
     * 表类型 1：表  2：视图
     */
    @ApiModelProperty(value = "表类型 1：表  2：视图")
    public int tableType;

    /**
     * 表业务类型 1：事实表、2：维度表、3、指标表  4、宽表
     */
    @ApiModelProperty(value = "表业务类型 1：事实表、2：维度表、3、指标表  4、宽表")
    public int tableBusinessType;

    /**
     * 生成规则（清洗脚本）
     */
    @ApiModelProperty(value = "生成规则（清洗脚本）")
    public String createRule;

    /**
     * 规则执行顺序
     */
    @ApiModelProperty(value = "规则执行顺序")
    public int ruleSort;

    /**
     * 规则状态
     */
    @ApiModelProperty(value = "规则状态")
    public RuleStateEnum ruleState;
}
