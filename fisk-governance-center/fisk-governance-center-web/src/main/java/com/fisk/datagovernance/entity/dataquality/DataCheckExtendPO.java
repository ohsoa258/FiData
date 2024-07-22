package com.fisk.datagovernance.entity.dataquality;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 数据校验规则扩展表
 * @date 2022/5/16 12:35
 */
@Data
@TableName("tb_datacheck_rule_extend")
public class DataCheckExtendPO extends BasePO {
    /**
     * 数据校验规则id
     */
    public int ruleId;

    /**
     * 字段名称/字段Id
     */
    public String fieldUnique;

    /**
     * 字段名称
     */
    public String fieldName;

    /**
     * 字段类型
     */
    public String fieldType;

    /**
     * 值域检查-类型：
     * 1、序列范围(枚举)
     * 2、取值范围(1~10)
     * 3、日期范围(2023-04-01 12:59:59~2023-05-01 12:00:00)
     * 4、关键字包含
     */
    public int rangeCheckType;

    /**
     * 值域检查-序列范围类型：
     * 1、值类型
     * 2、表字段类型
     */
    public int rangeType;

    /**
     * 值域检查-取值范围类型：1、单向取值 2、区间取值
     */
    public int rangeCheckValueRangeType;

    /**
     * 值域检查-关键字包含类型：1、包含关键字 2、前包含关键字 3、后包含关键字
     */
    public int rangeCheckKeywordIncludeType;

    /**
     * 值域检查-取值范围-单向取值-运算符
     */
    public String rangeCheckOneWayOperator;

    /**
     * 值域检查-对比值；
     */
    public String rangeCheckValue;

    /**
     * 规范检查-类型：
     * 1、日期格式
     * 2、字符范围
     * 3、URL地址(http或https或ftp或file开头)
     * 4、Base64字节流
     */
    public int standardCheckType;

    /**
     * 规范检查-字符范围类型：1、字符精度范围 2、字符长度范围
     */
    public int standardCheckCharRangeType;

    /**
     * 规范检查-日期格式值，多个日期格式逗号分隔
     */
    public String standardCheckTypeDateValue;

    /**
     * 规范检查-浮点长度范围分隔符
     */
    public String standardCheckTypeLengthSeparator;

    /**
     * 规范检查-字符长度范围运算符
     */
    public String standardCheckTypeLengthOperator;

    /**
     * 规范检查-字符长度值/浮点长度范围
     */
    public String standardCheckTypeLengthValue;

    /**
     * 规范检查-正则表达式
     */
    public String standardCheckTypeRegexpValue;

    /**
     * 波动检查-类型：SUM、COUNT、AVG、MAX、MIN
     */
    public int fluctuateCheckType;

    /**
     * 波动检查-运算符：>、<、>=、<=、=
     */
    public String fluctuateCheckOperator;

    /**
     * 波动检查-波动值
     */
    public double fluctuateCheckValue;

    /**
     * 血缘检查-类型：
     * 1、检查上游血缘是否断裂
     * 2、检查下游血缘是否断裂
     * 3、检查上下游血缘是否断裂
     */
    public int parentageCheckType;

    /**
     * 正则表达式检查-表达式值
     */
    public String regexpCheckValue;

    /**
     * SQL检查-脚本值
     */
    public String sqlCheckValue;

    /**
     * 是否记录错误数据：0 不记录、1  记录
     */
    public int recordErrorData;

    /**
     * 错误数据保留时间：7天、14天、30天
     */
    public int errorDataRetentionTime;

    public Integer checkDatabaseId;

    public String checkDatabaseName;

    public Integer checkTableId;

    public String checkTableName;

    public Integer checkFieldId;

    public String checkFieldName;

    /**
     * 指定查询的字段名称，多个字段逗号分隔
     */
    public String allocateFieldNames;
}
