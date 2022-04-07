package com.fisk.datagovernance.dto.dataquality.datacheck;

import com.fisk.datagovernance.dto.dataquality.notice.ComponentNotificationDTO;
import com.fisk.datagovernance.enums.dataquality.*;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 数据校验DTO
 * @date 2022/3/24 13:27
 */
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
     * 数据源类型
     */
    @ApiModelProperty(value = "数据源类型")
    public ModuleDataSourceTypeEnum datasourceType;

    /**
     * 组件名称
     */
    @ApiModelProperty(value = "组件名称")
    public String moduleName;

    /**
     * 检验步骤
     */
    @ApiModelProperty(value = "检验步骤")
    public CheckStepTypeEnum checkStep;

    /**
     * 表名称
     */
    @ApiModelProperty(value = "表名称")
    public String tableName;

    /**
     * 前置表名称
     */
    @ApiModelProperty(value = "前置表名称")
    public String proTableName;

    /**
     * 字段名称
     */
    @ApiModelProperty(value = "字段名称")
    public String fieldName;

    /**
     * 字段长度
     */
    @ApiModelProperty(value = "字段长度")
    public int fieldLength;

    /**
     * 字段聚合函数
     */
    @ApiModelProperty(value = "字段聚合函数")
    public String fieldAggregate;

    /**
     * 校验规则的类型，英文逗号分隔
     * 1、唯一校验
     * 2、非空校验
     * 3、长度校验
     */
    @ApiModelProperty(value = "校验规则的类型，英文逗号分隔")
    public String checkRuleType;

    /**
     * 波动阈值
     */
    @ApiModelProperty(value = "波动阈值")
    public Integer thresholdValue;

    /**
     * 表行数，实际表行数减去表行数
     */
    @ApiModelProperty(value = "表行数")
    public Integer rowsValue;

    /**
     * 运行时间表达式
     */
    @ApiModelProperty(value = "运行时间表达式")
    public String runTimeCron;

    /**
     * 上下游血缘关系范围
     * 1、上游 2、下游 3、上下游
     */
    @ApiModelProperty(value = "上下游血缘关系范围 1、上游 2、下游 3、上下游")
    public Integer checkConsanguinity;

    /**
     * 组件规则类型
     */
    @ApiModelProperty(value = "组件规则类型")
    public ModuleTypeEnum moduleType;

    /**
     * 组件规则
     */
    @ApiModelProperty(value = "组件规则")
    public String moduleRule;

    /**
     * 组件状态
     */
    @ApiModelProperty(value = "组件状态")
    public ModuleStateEnum moduleState;

    /**
     * 组件通知关联DTO
     */
    @ApiModelProperty(value = "组件通知关联DTO")
    public List<ComponentNotificationDTO> componentNotificationDTOS;

    /**
     * 数据校验模块下相似度组件扩展属性
     */
    @ApiModelProperty(value = "数据校验模块下相似度组件扩展属性")
    public List<SimilarityExtendDTO> similarityExtendDTOS;

}
