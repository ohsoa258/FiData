package com.fisk.datagovernance.dto.dataquality.datacheck;

import com.fisk.datagovernance.enums.dataquality.*;
import com.fisk.datagovernance.vo.dataquality.datacheck.DataCheckExtendVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 数据校验DTO
 * @date 2022/3/24 13:27
 */
@Data
public class DataCheckDTO {
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
     * 校验规则：1、强规则 2、弱规则
     */
    @ApiModelProperty(value = "校验规则")
    public String checkRule;

    /**
     * 生成规则
     */
    @ApiModelProperty(value = "生成规则")
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

    /**
     * 波动阈值
     */
    @ApiModelProperty(value = "波动阈值")
    public int thresholdValue;

    /**
     * 数据校验规则扩展属性
     */
    @ApiModelProperty(value = "数据校验规则扩展属性")
    public List<DataCheckExtendDTO> dataCheckExtends;
}
