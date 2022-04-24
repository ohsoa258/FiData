package com.fisk.datagovernance.vo.dataquality.lifecycle;

import com.fisk.datagovernance.enums.dataquality.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 生命周期VO
 * @date 2022/3/22 15:36
 */
@Data
public class LifecycleVO {
    /**
     * id
     */
    @ApiModelProperty(value = "id")
    public int id;

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
     * 表名称
     */
    @ApiModelProperty(value = "表名称")
    public String tableName;

    /**
     * 字段名称，更新依据字段
     */
    @ApiModelProperty(value = "字段名称,更新依据字段")
    public String fieldName;

    /**
     * 回收时间
     */
    @ApiModelProperty(value = "回收时间")
    public String recoveryDate;

    /**
     * 提醒时间
     */
    @ApiModelProperty(value = "提醒时间")
    public int remindDate;

    /**
     * 是否需要备份，默认否
     */
    @ApiModelProperty(value = "是否需要备份")
    public int isBackup;

    /**
     * 检查空表持续天数
     */
    @ApiModelProperty(value = "检查空表持续天数")
    public int checkEmptytbDay;

    /**
     * 检查表无刷新天数
     */
    @ApiModelProperty(value = "检查表无刷新天数")
    public int checkRefreshtbDay;

    /**
     * 检查表血缘断裂持续天数
     */
    @ApiModelProperty(value = "检查表血缘断裂持续天数")
    public int checkConsanguinityDay;

    /**
     * 上下游血缘关系范围：1、上游 2、下游 3、上下游
     */
    @ApiModelProperty(value = "上下游血缘关系范围：1、上游 2、下游 3、上下游")
    public int checkConsanguinity;

    /**
     * 运行时间表达式
     */
    @ApiModelProperty(value = "运行时间表达式")
    public String runTimeCron;

    /**
     * 表状态
     */
    @ApiModelProperty(value = "表状态")
    public TableStateTypeEnum tableState;

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
     * 模板名称
     */
    @ApiModelProperty(value = "模板名称")
    public String templatenName;

    /**
     * 模板模块
     */
    @ApiModelProperty(value = "模板模块")
    public TemplateModulesTypeEnum templateModules;

    /**
     * 模板类型
     */
    @ApiModelProperty(value = "模板类型")
    public TemplateTypeEnum templateType;

    /**
     * 模板描述
     */
    @ApiModelProperty(value = "模板描述")
    public String templateDesc;

    /**
     * 下次运行时间
     */
    @ApiModelProperty(value = "下次运行时间")
    public String nextTime;

    /**
     * 通知id集合
     */
    @ApiModelProperty(value = "通知id集合")
    public List<Integer> noticeIds;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    public LocalDateTime createTime;

    /**
     * 创建人
     */
    @ApiModelProperty(value = "创建人")
    public String createUser;
}
