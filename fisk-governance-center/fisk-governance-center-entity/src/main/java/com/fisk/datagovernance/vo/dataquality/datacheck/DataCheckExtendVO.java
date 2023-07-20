package com.fisk.datagovernance.vo.dataquality.datacheck;

import com.fisk.datagovernance.enums.dataquality.FluctuateCheckTypeEnum;
import com.fisk.datagovernance.enums.dataquality.ParentageCheckTypeEnum;
import com.fisk.datagovernance.enums.dataquality.RangeCheckTypeEnum;
import com.fisk.datagovernance.enums.dataquality.StandardCheckTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 数据校验规则扩展属性
 * @date 2022/4/2 11:07
 */
@Data
public class DataCheckExtendVO {
    /**
     * id
     */
    @ApiModelProperty(value = "id")
    public int id;

    /**
     * 数据校验规则id
     */
    @ApiModelProperty(value = "数据校验规则id")
    public int ruleId;

    /**
     * 字段名称/字段Id
     */
    @ApiModelProperty(value = "实际字段名称/字段Id")
    public String fieldUnique;

    /**
     * 字段名称
     */
    @ApiModelProperty(value = "字段名称")
    public String fieldName;

    /**
     * 字段类型
     */
    @ApiModelProperty(value = "字段类型")
    public String fieldType;

    /**
     * 值域检查-类型：
     * 1、序列范围(枚举)
     * 2、取值范围(1~10)
     * 3、日期范围(2023-04-01 12:59:59~2023-05-01 12:00:00)
     */
    @ApiModelProperty(value = "值域检查-类型")
    public RangeCheckTypeEnum rangeCheckType;

    /**
     * 值域检查-对比值；
     */
    @ApiModelProperty(value = "值域检查-对比值")
    public String rangeCheckValue;

    /**
     * 规范检查-类型：
     * 1、日期格式
     * 2、字符精度长度范围(1~10)
     * 3、URL地址(http或https或ftp或file开头)
     * 4、Base64字节流
     */
    @ApiModelProperty(value = "规范检查-类型")
    public StandardCheckTypeEnum standardCheckType;

    /**
     * 规范检查-日期格式值，多个日期格式逗号分隔
     */
    @ApiModelProperty(value = "规范检查-日期格式值，多个日期格式逗号分隔")
    public String standardCheckTypeDateValue;

    /**
     * 规范检查-字符和浮点长度范围分隔符(.)
     */
    @ApiModelProperty(value = "规范检查-字符和浮点长度范围分隔符(.)")
    public String standardCheckTypeLengthSeparator;

    /**
     * 规范检查-字符和浮点长度范围(1,10)
     */
    @ApiModelProperty(value = "规范检查-字符和浮点长度范围(1,10)")
    public String standardCheckTypeLengthValue;

    /**
     * 规范检查-正则表达式
     */
    @ApiModelProperty(value = "规范检查-正则表达式")
    public String standardCheckTypeRegexpValue;

    /**
     * 波动检查-类型：SUM、COUNT、AVG、MAX、MIN
     */
    @ApiModelProperty(value = "波动检查-类型：SUM、COUNT、AVG、MAX、MIN")
    public FluctuateCheckTypeEnum fluctuateCheckType;

    /**
     * 波动检查-运算符：>、<、>=、<=、=
     */
    @ApiModelProperty(value = "波动检查-运算符：>、<、>=、<=、=")
    public String fluctuateCheckOperator;

    /**
     * 波动检查-波动值
     */
    @ApiModelProperty(value = "波动检查-波动值")
    public double fluctuateCheckValue;

    /**
     * 血缘检查-类型：
     * 1、检查上游血缘是否断裂
     * 2、检查下游血缘是否断裂
     * 3、检查上下游血缘是否断裂
     */
    @ApiModelProperty(value = "血缘检查-类型")
    public ParentageCheckTypeEnum parentageCheckType;

    /**
     * 正则表达式检查-表达式值
     */
    @ApiModelProperty(value = "正则表达式检查-表达式值")
    public String regexpCheckValue;

    /**
     * SQL检查-脚本值(固定返回checkstate，通过为1，未通过为0)
     */
    @ApiModelProperty(value = "SQL检查-脚本值(固定返回checkstate，通过为1，未通过为0)")
    public String sqlCheckValue;

    /**
     * 是否记录错误数据：0 不记录、1  记录
     */
    @ApiModelProperty(value = "是否记录错误数据：0 不记录、1  记录")
    public int recordErrorData;

    /**
     * 错误数据保留时间：7天、14天、30天
     */
    @ApiModelProperty(value = "错误数据保留时间：7天、14天、30天")
    public int errorDataRetentionTime;
}
