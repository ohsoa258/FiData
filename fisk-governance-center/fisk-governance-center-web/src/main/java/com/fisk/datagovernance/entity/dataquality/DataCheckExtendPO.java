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
     * 3、日期范围(20230401125959~20230501120000)
     */
    public int rangeCheckType;

    /**
     * 值域检查-对比值；
     */
    public String rangeCheckValue;

    /**
     * 规范检查-类型：
     * 1、日期格式
     * 2、字符精度长度范围(1~10)
     * 3、URL地址(http或https或ftp或file开头)
     * 4、Base64字节流
     */
    public int standardCheckType;

    /**
     * 规范检查-日期格式值，多个日期格式逗号分隔
     */
    public String standardCheckTypeDateValue;

    /**
     * 规范检查-字符和浮点长度范围分隔符(.)
     */
    public String standardCheckTypeLengthSeparator;

    /**
     * 规范检查-字符和浮点长度范围(1,10)
     */
    public String standardCheckTypeLengthValue;

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
     * SQL检查-脚本值(固定返回checkstate，通过为1，未通过为0)
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
}
