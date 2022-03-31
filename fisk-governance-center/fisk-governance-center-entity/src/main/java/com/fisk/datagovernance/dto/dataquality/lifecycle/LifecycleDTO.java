package com.fisk.datagovernance.dto.dataquality.lifecycle;

import com.fisk.datagovernance.dto.dataquality.notice.ComponentNotificationDTO;
import com.fisk.datagovernance.enums.dataquality.ModuleDataSourceTypeEnum;
import com.fisk.datagovernance.enums.dataquality.ModuleStateEnum;
import com.fisk.datagovernance.enums.dataquality.TableStateTypeEnum;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 生命周期DTO
 * @date 2022/3/24 13:59
 */
public class LifecycleDTO {
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
     * 组件状态
     */
    @ApiModelProperty(value = "组件状态")
    public ModuleStateEnum moduleState;

    /**
     * 组件通知关联DTO
     */
    @ApiModelProperty(value = "组件通知关联DTO")
    public List<ComponentNotificationDTO> componentNotificationDTOS;
}
