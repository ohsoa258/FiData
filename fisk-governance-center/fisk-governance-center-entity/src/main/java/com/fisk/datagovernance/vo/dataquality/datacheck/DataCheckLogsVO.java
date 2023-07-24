package com.fisk.datagovernance.vo.dataquality.datacheck;

import com.alibaba.fastjson.JSONArray;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 数据校验日志
 * @date 2022/4/2 11:07
 */
@Data
public class DataCheckLogsVO {
    /**
     * 数据校验规则id
     */
    @ApiModelProperty(value = "id")
    public int ruleId;

    /**
     * 模板id
     */
    @ApiModelProperty(value = "id")
    public int templateId;

    /**
     * 检查模板名称
     */
    @ApiModelProperty(value = "id")
    public String checkTemplateName;

    /**
     * 平台数据源表主键id
     */
    @ApiModelProperty(value = "id")
    public int fiDatasourceId;

    /**
     * 日志类型：
     * 1 接口同步数据校验日志（同步前）
     * 2 nifi同步数据校验日志（同步中）
     * 3 订阅报告数据校验日志（同步后）
     */
    @ApiModelProperty(value = "id")
    public int logType;

    /**
     * 表架构名称
     */
    @ApiModelProperty(value = "id")
    public String schemaName;

    /**
     * 表名称
     */
    @ApiModelProperty(value = "id")
    public String tableName;

    /**
     * 字段名称
     */
    @ApiModelProperty(value = "id")
    public String fieldName;

    /**
     * 检查数据的批次号
     */
    @ApiModelProperty(value = "id")
    public String checkBatchNumber;

    /**
     * 检查数据的小批次号
     */
    @ApiModelProperty(value = "id")
    public String checkSmallBatchNumber;

    /**
     * 检查数据的总条数
     */
    @ApiModelProperty(value = "id")
    public String checkTotalCount;

    /**
     * 检查数据不通过的条数
     */
    @ApiModelProperty(value = "id")
    public String checkFailCount;

    /**
     * 检查结果
     */
    @ApiModelProperty(value = "id")
    public String checkResult;

    /**
     * 检查提示消息
     */
    @ApiModelProperty(value = "id")
    public String checkMsg;

    /**
     * 错误Json数据
     */
    @ApiModelProperty(value = "id")
    public JSONArray errorData;
}
