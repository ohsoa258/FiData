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
     * 表名称/表Id
     */
    public String tableUnique;

    /**
     * 表类型 1：表  2：视图
     */
    public int tableType;

    /**
     * 校验规则：1、强规则 2、弱规则
     */
    public int checkRule;

    /**
     * 生成规则
     */
    public String createRule;

    /**
     * 规则执行顺序
     */
    public int ruleSort;

    /**
     * 规则状态：1、启用 0、禁用
     */
    public int ruleState;

    /**
     * 波动阈值
     */
    public int thresholdValue;
}


