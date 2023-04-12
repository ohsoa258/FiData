package com.fisk.datagovernance.vo.dataquality.businessfilter;

import com.fisk.datagovernance.enums.dataquality.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 业务清洗规则VO
 * @date 2022/3/22 15:36
 */
@Data
public class BusinessFilterVO {
    /**
     * id
     */
    @ApiModelProperty(value = "主键id")
    public int id;

    /**
     * 数据质量数据源表主键ID
     */
    @ApiModelProperty(value = "数据质量数据源表主键ID")
    public int datasourceId;

    /**
     * FiData系统数据源表主键ID
     */
    @ApiModelProperty(value = "FiData系统数据源表主键ID")
    public int fiDataSourceId;

    /**
     * 数据源类型 1:FiData 2:custom
     */
    @ApiModelProperty(value = "数据源类型 1:FiData 2:custom")
    public SourceTypeEnum sourceTypeEnum;

    /**
     * 规则名称
     */
    @ApiModelProperty(value = "规则名称")
    public String ruleName;

    /**
     * custom模式下是表名称
     * FiData类型下是表Id
     */
    @ApiModelProperty(value = "custom模式下是表名称/FiData类型下是表Id")
    public String tableUnique;

    /**
     * 表名称
     */
    @ApiModelProperty(value = "表名称")
    public String tableName;

    /**
     * 表别名
     */
    @ApiModelProperty(value = "表别名")
    public String tableAlias;

    /**
     * 表类型 1：表  2：视图
     */
    @ApiModelProperty(value = "表类型 1：表  2：视图")
    public int tableType;

    /**
     * 表业务类型 1：dw维度表、2：dw事实表、3、doris维度表  4、doris事实表 5、宽表
     */
    @ApiModelProperty(value = "表业务类型 1：dw维度表、2：dw事实表、3、doris维度表  4、doris事实表 5、宽表")
    public int tableBusinessType;

    /**
     * 规则执行顺序
     */
    @ApiModelProperty(value = "规则执行顺序")
    public int ruleSort;

    /**
     * 规则状态
     */
    @ApiModelProperty(value = "规则状态")
    public RuleStateEnum ruleState;

    /**
     * 规则描述
     */
    @ApiModelProperty(value = "规则描述")
    public String ruleDescribe;

    /**
     * 清洗场景：1 同步前 2 同步中 3 同步后
     */
    @ApiModelProperty(value = "清洗场景：1 同步前 2 同步中 3 同步后")
    public int filterScene;

    /**
     * 触发场景：1 调度任务 2 质量报告 3 暂无
     */
    @ApiModelProperty(value = "触发场景：1 调度任务 2 质量报告 3 暂无")
    public int triggerScene;

    /**
     * 序号
     */
    @ApiModelProperty(value = "序号")
    public int orderNumber;
}
