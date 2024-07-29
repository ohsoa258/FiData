package com.fisk.datagovernance.vo.dataquality.datacheck;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 数据校验规则搜索条件
 * @date 2022/4/12 18:17
 */
@Data
public class DataCheckRuleSearchWhereVO {
    /**
     * 检查环节
     */
    @ApiModelProperty(value = "检查环节")
    public List<DataCheckRuleSearchWhereMapVO> checkProcessMap;

    /**
     * 表全名称（含架构名）
     */
    @ApiModelProperty(value = "表全名称（含架构名）")
    public List<DataCheckRuleSearchWhereMapVO> tableFullNameMap;

    /**
     * 报告批次号
     */
    @ApiModelProperty(value = "报告批次号")
    public List<DataCheckRuleSearchWhereMapVO> reportBatchNumberMap;

    /**
     * 数据校验-规则模板
     */
    @ApiModelProperty(value = "数据校验-规则模板")
    public List<DataCheckRuleSearchWhereMapVO> templateMap;
}
