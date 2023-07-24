package com.fisk.datagovernance.vo.dataquality.datacheck;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 数据校验结果
 * @date 2022/4/12 18:17
 */
@Data
public class DataCheckResultVO
{
    /**
     * 规则id
     */
    @ApiModelProperty(value = "规则id")
    public int ruleId;

    /**
     * 规则名称
     */
    @ApiModelProperty(value = "规则名称")
    public String ruleName;

    /**
     * 检查模板名称
     */
    @ApiModelProperty(value = "检查模板名称")
    public String checkTemplateName;

    /**
     * 检查模板描述
     */
    @ApiModelProperty(value = "检查模板描述")
    public String checkTemplateDesc;

    /**
     * 检查的库
     */
    @ApiModelProperty(value = "检查的库")
    public String checkDataBase;

    /**
     * 检查的架构
     */
    @ApiModelProperty(value = "检查的架构")
    public String checkSchema;

    /**
     * 检查的表
     */
    @ApiModelProperty(value = "检查的表")
    public String checkTable;

    /**
     * 检查的表标识
     */
    @ApiModelProperty(value = "检查的表标识")
    public String checkTableUnique;

    /**
     * 检查的字段
     */
    @ApiModelProperty(value = "检查的字段")
    public String checkField;

    /**
     * 检查的字段标识
     */
    @ApiModelProperty(value = "检查的字段标识")
    public String checkFieldUnique;

    /**
     * 检查类型：强规则/弱规则
     */
    @ApiModelProperty(value = "检查类型：强规则/弱规则")
    public String checkType;

    /**
     * 检查数据的总条数
     */
    @ApiModelProperty(value = "检查数据的总条数")
    public String checkTotalCount;

    /**
     * 检查数据不通过的条数
     */
    @ApiModelProperty(value = "检查数据不通过的条数")
    public String checkFailCount;

    /**
     * 检查的结果
     */
    @ApiModelProperty(value = "检查的结果")
    public Object checkResult;

    /**
     * 检查的结果消息
     */
    @ApiModelProperty(value = "检查的结果消息")
    public String checkResultMsg;

    /**
     * 检查的错误Json数据
     */
    @ApiModelProperty(value = "检查的错误Json数据")
    public String checkErrorData;

    /**
     * 修改SQL语句
     */
    @ApiModelProperty(value = "修改SQL语句")
    public String updateSql;
}
