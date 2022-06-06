package com.fisk.datagovernance.dto.dataquality.businessfilter;

import com.fisk.datagovernance.enums.dataquality.RuleStateEnum;
import io.swagger.annotations.ApiModelProperty;


/**
 * @author dick
 * @version 1.0
 * @description 业务清洗DTO
 * @date 2022/3/24 13:48
 */
public class BusinessFilterDTO {
    /**
     * 模板id
     */
    @ApiModelProperty(value = "模板id")
    public int templateId;

    /**
     * 数据源id
     */
    @ApiModelProperty(value = "数据源id")
    public int datasourceId;

    /**
     * 规则名称
     */
    @ApiModelProperty(value = "规则名称")
    public String ruleName;

    /**
     * 表名称，页面展示
     */
    @ApiModelProperty(value = "表名称，页面展示")
    public String tableName;

    /**
     * 实际引用表名称
     */
    @ApiModelProperty(value = "实际引用表名称")
    public String useTableName;

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
