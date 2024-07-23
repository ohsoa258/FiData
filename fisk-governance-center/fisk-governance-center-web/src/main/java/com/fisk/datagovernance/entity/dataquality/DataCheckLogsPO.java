package com.fisk.datagovernance.entity.dataquality;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 数据校验日志
 * @date 2023/7/7 14:48
 */
@Data
@TableName("tb_datacheck_rule_logs")
public class DataCheckLogsPO extends BasePO {
    /**
     * 数据校验规则id
     */
    public int ruleId;

    /**
     * uuid,用于关联附件表的object_id
     */
    public String idUuid;

    /**
     * 数据校验规则名称
     */
    public String ruleName;

    /**
     * 模板id
     */
    public int templateId;

    /**
     * 检查模板名称
     */
    public String checkTemplateName;

    /**
     * 平台数据源表主键id
     */
    public int fiDatasourceId;

    /**
     * 日志类型：
     * 1 接口同步数据校验日志（同步前）
     * 2 nifi同步数据校验日志（同步中）
     * 3 订阅报告规则校验日志（同步后）
     */
    public int logType;

    /**
     * 表架构名称
     */
    public String schemaName;

    /**
     * 表名称
     */
    public String tableName;

    /**
     * 字段名称
     */
    public String fieldName;

    /**
     * 检查数据的批次号
     */
    public String checkBatchNumber;

    /**
     * 检查数据的小批次号
     */
    public String checkSmallBatchNumber;

    /**
     * 检查数据的总条数
     */
    public String checkTotalCount;

    /**
     * 检查数据不通过的条数
     */
    public String checkFailCount;

    /**
     * 检查结果
     */
    public String checkResult;

    /**
     * 检查提示消息
     */
    public String checkMsg;

    /**
     * 检查规则说明
     */
    public String checkRuleIllustrate;

    /**
     * 错误Json数据
     */
    public String errorData;

    /**
     * 检查数据的正确率
     */
    public String checkDataAccuracy;

    /**
     * 检查数据开始时间
     */
    public String checkDataStartTime;

    /**
     * 检查数据结束时间
     */
    public String checkDataEndTime;

    /**
     * 检查数据所需时长，单位：秒
     */
    public String checkDataDuration;

    /**
     * 检查数据的SQL语句
     */
    public String checkDataSql;

    /**
     * 检查数据总条数的SQL语句
     */
    public String checkDataCountSql;
}
