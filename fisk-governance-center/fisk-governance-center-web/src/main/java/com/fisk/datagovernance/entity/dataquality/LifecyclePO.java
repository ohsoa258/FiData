package com.fisk.datagovernance.entity.dataquality;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 生命周期组件表
 * @date 2022/3/22 15:12
 */
@Data
@TableName("tb_lifecycle_module")
public class LifecyclePO extends BasePO {
    /**
     * 模板id
     */
    public int templateId;

    /**
     * 数据源id
     */
    public int datasourceId;

    /**
     * 数据源类型
     */
    public int datasourceType;

    /**
     * 组件名称
     */
    public String moduleName;

    /**
     * 表名称
     */
    public String tableName;

    /**
     * 字段名称，更新依据字段
     */
    public String fieldName;

    /**
     * 回收时间
     */
    public String recoveryDate;

    /**
     * 提醒时间
     */
    public int remindDate;

    /**
     * 是否需要备份，默认否
     */
    public int isBackup;

    /**
     * 检查空表持续天数
     */
    public int checkEmptytbDay;

    /**
     * 检查表无刷新天数
     */
    public int checkRefreshtbDay;

    /**
     * 检查表血缘断裂持续天数
     */
    public int checkConsanguinityDay;

    /**
     * 上下游血缘关系范围：1、上游 2、下游 3、上下游
     */
    public int checkConsanguinity;

    /**
     * 运行时间表达式
     */
    public String runTimeCron;

    /**
     * 表状态
     */
    public int tableState;

    /**
     * 组件规则
     */
    public String moduleRule;

    /**
     * 组件状态
     */
    public int moduleState;
}
