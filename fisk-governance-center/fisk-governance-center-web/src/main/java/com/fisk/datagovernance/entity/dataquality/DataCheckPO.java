package com.fisk.datagovernance.entity.dataquality;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * @author dick
 * @version v1.0
 * @description 数据校验规则表
 * @date 2022/3/22 14:51
 */
@Data
@TableName("tb_datacheck_rule")
public class DataCheckPO extends BasePO {
    /**
     * 模板id
     */
    public int templateId;

    /**
     * 数据源表主键id
     */
    public int datasourceId;

    /**
     * 规则名称
     */
    public String ruleName;

    /**
     * 架构名称
     */
    public String schemaName;

    /**
     * 表名称/表Id
     */
    public String tableUnique;

    /**
     * 表名称
     */
    public String tableName;

    /**
     * 表类型 1：表  2：视图
     */
    public int tableType;

    /**
     * 表业务类型 1：事实表、2：维度表、3、指标表  4、宽表
     */
    public int tableBusinessType;

    /**
     * 校验规则：1、强规则 2、弱规则
     */
    public int ruleCheckType;

    /**
     * 规则执行节点：1、同步前 2、同步中 3、同步后
     */
    public int ruleExecuteNode;

    /**
     * 规则执行顺序
     */
    public int ruleExecuteSort;

    /**
     * 规则权重，用于统计分析
     */
    public int ruleWeight;

    /**
     * 规则描述
     */
    public String ruleDescribe;

    /**
     * 规则状态：1、启用 0、禁用
     */
    public int ruleState;

    /**
     * 规则说明
     */
    public String ruleIllustrate;
}


