package com.fisk.datagovernance.vo.dataquality.datacheck;

import com.fisk.datagovernance.enums.dataquality.CheckRuleEnum;
import com.fisk.datagovernance.enums.dataquality.CheckTypeEnum;
import com.fisk.datagovernance.enums.dataquality.DataCheckTypeEnum;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 同步校验
 * @date 2022/6/1 12:47
 */
@Data
public class SyncCheckInfoVO {
    /**
     * 字段校验模板；
     * 校验类型，多选逗号分割：
     * 1、唯一校验
     * 2、非空校验
     * 3、数据校验
     */
    public CheckTypeEnum checkType;

    /**
     * 数据校验类型：
     * 1、文本长度校验
     * 2、日期格式校验
     * 3、序列范围校验
     */
    public DataCheckTypeEnum dataCheckType;

    /**
     * 校验类型名称
     */
    public String checkTypeName;

    /**
     * 表名称
     */
    public String tableName;

    /**
     * 表标识
     */
    public String tableUnique;

    /**
     * 字段名称
     */
    public String fieldName;

    /**
     * 字段标识
     */
    public String fieldUnique;

    /**
     * 校验表
     */
    public String checkTable;

    /**
     * 校验字段
     */
    public String checkField;

    /**
     * 校验字段条件
     */
    public String checkFieldWhere;

    /**
     * 修改时的sql条件
     */
    public String sqlWhere;

    /**
     * 修改时的sql
     */
    public String updateSql;

    /**
     * 校验的结果
     */
    public boolean checkResult;

    /**
     * 校验的结果消息
     */
    public String checkResultMsg;

    /**
     * 校验规则 1：强规则，2：若规则
     */
    public CheckRuleEnum checkRule;

    /**
     * 规则id
     */
    public int ruleId;

    /**
     * 规则名称
     */
    public String ruleName;

    /**
     * 检查的库
     */
    public String checkDataBase;

    /**
     * 检查的描述
     */
    public String checkDesc;
}
