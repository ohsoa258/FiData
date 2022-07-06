package com.fisk.datagovernance.entity.dataquality;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 生命周期规则表
 * @date 2022/3/22 15:12
 */
@Data
@TableName("tb_lifecycle_rule")
public class LifecyclePO extends BasePO {
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
     * 回收时间
     */
    public String recoveryDate;

    /**
     * 已持续次数
     */
    public int continuedNumber;

    /**
     * 是否需要备份，默认否
     */
    public int isBackup;

    /**
     * 数据血缘断裂回收模板；
     * 上下游血缘关系范围：
     * 1、上游 2、下游 3、上下游
     */
    public int consanguinityRange;

    /**
     * 数据无刷新模板；
     * 字段名称
     */
    public String fieldName;

    /**
     * 表状态：1、正常 0、回收
     */
    public int tableState;

    /**
     * 生成规则
     */
    public String createRule;

    /**
     * 规则状态：1、启用 0、禁用
     */
    public int ruleState;
}
