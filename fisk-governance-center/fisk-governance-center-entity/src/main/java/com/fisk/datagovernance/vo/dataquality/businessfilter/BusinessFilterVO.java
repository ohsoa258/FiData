package com.fisk.datagovernance.vo.dataquality.businessfilter;

import com.fisk.datagovernance.enums.dataquality.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 业务清洗VO
 * @date 2022/3/22 15:36
 */
@Data
public class BusinessFilterVO {
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
     * 清洗步骤
     */
    @ApiModelProperty(value = "清洗步骤")
    public CheckStepTypeEnum filterStep;

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
