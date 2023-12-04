package com.fisk.dataservice.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
@TableName("tb_table_sync_mode")
public class TableSyncModePO extends BasePO {

    /**
     *
     */
    public Integer type;

    /**
     * 类型表id
     */
    public Integer typeTableId;

    /**
     * timer driver
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    public String timerDriver;

    /**
     * corn表达式
     */
    public String cornExpression;

    /**
     * 单个数据流文件加载最大数据行
     */
    public Integer maxRowsPerFlowFile;

    /**
     * 单词从结果集中提取的最大数据行
     */
    public Integer fetchSize;

    /**
     * 触发类型:1定时触发 2关联触发
     */
    public Integer triggerType;

    /**
     * 关联管道
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    public Integer associatePipe;

    /**
     * 关联数据接入
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    public Integer associateInput;

    /**
     * 调度类型:1TIMER DRIVEN 2CRON DRIVEN
     */
    public Integer scheduleType;

    /**
     * 自定义脚本执行前
     */
    public String customScriptBefore;

    /**
     * 自定义脚本执行后
     */
    public String customScriptAfter;

}
