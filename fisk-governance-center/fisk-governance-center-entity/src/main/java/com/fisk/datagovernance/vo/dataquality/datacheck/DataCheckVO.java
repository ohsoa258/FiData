package com.fisk.datagovernance.vo.dataquality.datacheck;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fisk.common.core.enums.fidatadatasource.TableBusinessTypeEnum;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataTreeDTO;
import com.fisk.datagovernance.enums.dataquality.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 数据校验规则VO
 * @date 2022/3/22 15:35
 */
@Data
public class DataCheckVO {
    /**
     * 主键id
     */
    @ApiModelProperty(value = "主键id")
    public int id;

    /**
     * 模板id
     */
    @ApiModelProperty(value = "模板id")
    public int templateId;

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
     * 数据源类型
     */
    @ApiModelProperty(value = "数据源类型")
    public SourceTypeEnum sourceType;

    /**
     * 规则名称
     */
    @ApiModelProperty(value = "规则名称")
    public String ruleName;

    /**
     * 架构名称
     */
    @ApiModelProperty(value = "架构名称")
    public String schemaName;

    /**
     * 表名称/表Id
     */
    @ApiModelProperty(value = "表名称/表Id")
    public String tableUnique;

    /**
     * 表名称
     */
    @ApiModelProperty(value = "表名称")
    public String tableName;

    /**
     * 表描述
     */
    @ApiModelProperty(value = "表描述")
    public String tableDescribe;

    /**
     * 表别名
     */
    @ApiModelProperty(value = "表别名")
    public String tableAlias;

    /**
     * 表类型
     */
    @ApiModelProperty(value = "表类型")
    public TableTypeEnum tableType;

    /**
     * 表业务类型 1：事实表、2：维度表、3：指标表、4：宽表
     */
    @ApiModelProperty(value = "表业务类型 1：事实表、2：维度表、3：指标表、4：宽表")
    public TableBusinessTypeEnum tableBusinessType;

    /**
     * 规则检查类型：1、强规则 2、弱规则
     */
    @ApiModelProperty(value = "规则检查类型：1、强规则 2、弱规则")
    public RuleCheckTypeEnum ruleCheckType;

    /**
     * 规则执行节点：1、同步前 2、同步中 3、同步后
     */
    @ApiModelProperty(value = "规则执行节点：1、同步前 2、同步中 3、同步后")
    public RuleExecuteNodeTypeEnum ruleExecuteNode;

    /**
     * 规则执行顺序
     */
    @ApiModelProperty(value = "规则执行顺序")
    public int ruleExecuteSort;

    /**
     * 规则权重
     */
    @ApiModelProperty(value = "规则权重")
    public int ruleWeight;

    /**
     * 规则描述
     */
    @ApiModelProperty(value = "规则描述")
    public String ruleDescribe;

    /**
     * 规则状态：1、启用 0、禁用
     */
    @ApiModelProperty(value = "规则状态：1、启用 0、禁用")
    public RuleStateEnum ruleState;

    /**
     * 规则说明
     */
    @ApiModelProperty(value = "规则说明")
    public String ruleIllustrate;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public LocalDateTime createTime;

    /**
     * 创建人
     */
    @ApiModelProperty(value = "创建人")
    public String createUser;

    /**
     * 数据校验规则扩展属性
     */
    @ApiModelProperty(value = "数据校验规则扩展属性")
    public DataCheckExtendVO dataCheckExtend;

    /**
     * 数据校验规则检查条件
     */
    @ApiModelProperty(value = "数据校验规则检查条件")
    List<DataCheckConditionVO> dataCheckCondition;

    /**
     * 模板类型
     */
    @ApiModelProperty(value = "模板类型")
    public TemplateTypeEnum templateType;

    /**
     * 模板名称
     */
    @ApiModelProperty(value = "模板名称")
    public String templateName;

    /**
     * 规则所属分组id(数据标准)
     */
    @ApiModelProperty(value = "规则所属分组id(数据标准)")
    public Integer datacheckGroupId;

    /**
     * 规则所属报告
     */
    @ApiModelProperty(value = "规则所属报告")
    public List<String> belongToReportNameList;

    /**
     * 表字段信息
     */
    @ApiModelProperty(value = "表字段信息")
    public FiDataMetaDataTreeDTO tableFieldList;
}
