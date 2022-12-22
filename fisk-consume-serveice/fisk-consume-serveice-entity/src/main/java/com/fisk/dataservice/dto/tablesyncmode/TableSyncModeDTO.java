package com.fisk.dataservice.dto.tablesyncmode;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class TableSyncModeDTO {

    public long id;
    /**
     * 类型：1api服务 2表服务 3 文件服务
     */
    @ApiModelProperty(value = "类型：1api服务 2表服务 3 文件服务")
    public Integer type;

    /**
     * 类型表id
     */
    @ApiModelProperty(value = "类型id")
    public Integer typeTableId;

    /**
     * 1：全量、2：时间戳增量、3：业务时间覆盖、4：自定义覆盖；
     *//*
    @ApiModelProperty(value = "同步方法")
    public Integer syncMode;

    *//**
     * 时间戳字段
     *//*
    @ApiModelProperty(value = "时间戳字段")
    public String syncField;

    *//**
     * 自定义删除条件：定义每次同步的时候删除我们已有的数据条件
     *//*
    @ApiModelProperty(value = "自定义删除条件：定义每次同步的时候删除我们已有的数据条件")
    public String customDeleteCondition;

    *//**
     * 自定义插入条件：定义删除之后获取插入条件的数据进行插入
     *//*
    @ApiModelProperty(value = "自定义插入条件：定义删除之后获取插入条件的数据进行插入")
    public String customInsertCondition;*/

    /**
     * timer driver
     */
    @ApiModelProperty(value = "timer driver")
    public String timerDriver;

    /**
     * corn表达式
     */
    @ApiModelProperty(value = "corn表达式")
    public String cornExpression;

    /**
     * 保留历史数据 0 不保留历史版本 1 保留历史版本
     *//*
    @ApiModelProperty(value = "保留历史数据 0 不保留历史版本 1 保留历史版本")
    public int retainHistoryData;

    *//**
     * 保留时间
     *//*
    @ApiModelProperty(value = "保留时间")
    public int retainTime;

    *//**
     * 保留单位 年/季/月/周/日
     *//*
    @ApiModelProperty(value = "保留单位 年/季/月/周/日")
    public String retainUnit;

    *//**
     * 版本单位 年/季/月/周/日/自定义
     *//*
    @ApiModelProperty(value = "版本单位 年/季/月/周/日/自定义")
    public String versionUnit;

    *//**
     * 版本自定义规则
     *//*
    @ApiModelProperty(value = "版本自定义规则")
    public String versionCustomRule;*/

    /**
     * 单个数据流文件加载最大数据行
     */
    @ApiModelProperty(value = "单个数据流文件加载最大数据行")
    public Integer maxRowsPerFlowFile;

    /**
     * 单词从结果集中提取的最大数据行
     */
    @ApiModelProperty(value = "单词从结果集中提取的最大数据行")
    public Integer fetchSize;

    /**
     * 触发类型:1定时触发 2关联触发
     */
    @ApiModelProperty(value = "触发类型:1定时触发 2关联触发")
    public Integer triggerType;

    /**
     * 关联管道
     */
    @ApiModelProperty(value = "关联管道id")
    public Integer associatePipe;

    /**
     * 调度类型:1TIMER DRIVEN 2CRON DRIVEN
     */
    @ApiModelProperty(value = "调度类型:1TIMER DRIVEN 2CRON DRIVEN")
    public Integer scheduleType;

    /**
     * 自定义脚本执行前
     */
    @ApiModelProperty(value = "自定义脚本执行前")
    public String customScriptBefore;

    /**
     * 自定义脚本执行后
     */
    @ApiModelProperty(value = "自定义脚本执行后")
    public String customScriptAfter;

}
