package com.fisk.datagovernance.vo.dataquality.businessfilter;

import com.fisk.datagovernance.enums.dataquality.*;
import com.fisk.datagovernance.vo.dataquality.businessfilter.apifilter.BusinessFilterQueryApiVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

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
    @ApiModelProperty(value = "id")
    public int id;

    /**
     * 模板id
     */
    @ApiModelProperty(value = "模板id")
    public int templateId;

    /**
     * 数据源表主键id
     */
    @ApiModelProperty(value = "数据源表主键id")
    public int datasourceId;

    /**
     * 数据源类型
     */
    @ApiModelProperty(value = "数据源类型")
    public SourceTypeEnum sourceTypeEnum;

    /**
     * 规则名称
     */
    @ApiModelProperty(value = "规则名称")
    public String ruleName;

    /**
     * 表名称/表Id
     */
    @ApiModelProperty(value = "表名称/表Id")
    public String tableUnique;

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
     * 生成规则（清洗脚本）
     */
    @ApiModelProperty(value = "生成规则（清洗脚本）")
    public String createRule;

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
     * 模板应用场景
     */
    @ApiModelProperty(value = "模板应用场景")
    public TemplateSceneEnum templateScene;

    /**
     * 应用场景描述
     */
    @ApiModelProperty(value = "应用场景描述")
    public String sceneDesc;

    /**
     * 模板名称
     */
    @ApiModelProperty(value = "模板名称")
    public String templateName;

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
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    public LocalDateTime createTime;

    /**
     * 创建人
     */
    @ApiModelProperty(value = "创建人")
    public String createUser;

    /**
     * API信息
     */
    @ApiModelProperty(value = "API信息")
    public BusinessFilterQueryApiVO apiInfo;
}
