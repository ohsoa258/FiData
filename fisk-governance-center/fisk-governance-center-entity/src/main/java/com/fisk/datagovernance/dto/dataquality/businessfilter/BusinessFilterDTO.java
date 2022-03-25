package com.fisk.datagovernance.dto.dataquality.businessfilter;

import com.fisk.datagovernance.dto.dataquality.notice.ComponentNotificationDTO;
import com.fisk.datagovernance.enums.dataquality.CheckStepTypeEnum;
import com.fisk.datagovernance.enums.dataquality.ModuleDataSourceTypeEnum;
import com.fisk.datagovernance.enums.dataquality.ModuleStateEnum;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description TDDD
 * @date 2022/3/24 13:48
 */
public class BusinessFilterDTO {
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
     * 清洗步骤
     */
    @ApiModelProperty(value = "清洗步骤 1：进表前 2：进表后")
    public int filterStep;

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
     * 组件规则（清洗脚本）
     */
    @ApiModelProperty(value = "组件规则（清洗脚本）")
    public String moduleRule;

    /**
     * 运行时间表达式
     */
    @ApiModelProperty(value = "运行时间表达式")
    public String runTimeCron;

    /**
     * 组件执行顺序
     */
    @ApiModelProperty(value = "组件执行顺序")
    public int moduleExecSort;

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
