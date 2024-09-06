package com.fisk.datagovernance.dto.dataquality.datacheck;

import com.fisk.datagovernance.enums.dataquality.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2024-04-23
 * @Description:
 */
@Data
public class DatacheckStandardsGroupDTO {

    private Integer id;

    @ApiModelProperty(value = "校验组名称")
    private String checkGroupName;

    @ApiModelProperty(value = "数据标准MenuId")
    private Integer standardsMenuId;

    @ApiModelProperty(value = "数据元标准id")
    private Integer standardsId;

    @ApiModelProperty(value = "属性中文名称")
    private String chineseName;

    @ApiModelProperty(value = "属性英文名称")
    private String englishName;

    @ApiModelProperty(value = "描述")
    private String description;

    @ApiModelProperty(value = "字段类型")
    private String fieldType;

    @ApiModelProperty(value = "数据元编号")
    private String datametaCode;

    @ApiModelProperty(value = "规则检查类型：1、强规则 2、弱规则")
    public RuleCheckTypeEnum ruleCheckType;
    /**
     * 规则执行节点：1、同步前 2、同步中 3、同步后
     */
    @ApiModelProperty(value = "规则执行节点：1、同步前 2、同步中 3、同步后")
    public RuleExecuteNodeTypeEnum ruleExecuteNode;

    /**
     * 模板id
     */
    @ApiModelProperty(value = "模板id")
    public int templateId;

    /**
     * 值域检查-类型：
     * 1、序列范围(枚举)
     * 2、取值范围(1~10)
     * 3、日期范围(2023-04-01 12:59:59~2023-05-01 12:00:00)
     * 4、关键字包含
     */
    @ApiModelProperty(value = "值域检查-类型")
    public RangeCheckTypeEnum rangeCheckType;

    /**
     * 值域检查-序列范围类型：
     * 1、值类型
     * 2、表字段类型
     */
    @ApiModelProperty(value = "值域检查-参数类型")
    public int rangeType;

    /**
     * 值域检查-取值范围类型：1、单向取值 2、区间取值
     */
    @ApiModelProperty(value = "值域检查-取值范围类型")
    public RangeCheckValueRangeTypeEnum rangeCheckValueRangeType;

    /**
     * 值域检查-关键字包含类型：1、包含关键字 2、前包含关键字 3、后包含关键字
     */
    @ApiModelProperty(value = "值域检查-关键字包含类型")
    public RangeCheckKeywordIncludeTypeEnum rangeCheckKeywordIncludeType;

    /**
     * 值域检查-取值范围-单向取值-运算符
     */
    @ApiModelProperty(value = "值域检查-取值范围-单向取值-运算符")
    public String rangeCheckOneWayOperator;

    /**
     * 值域检查-对比值；
     */
    @ApiModelProperty(value = "值域检查-对比值")
    public String rangeCheckValue;

    /**
     * 规范检查-类型：
     * 1、日期格式
     * 2、字符范围
     * 3、URL地址(http或https或ftp或file开头)
     * 4、Base64字节流
     */
    @ApiModelProperty(value = "规范检查-类型")
    public StandardCheckTypeEnum standardCheckType;

    /**
     * 规范检查-字符范围类型：1、字符精度范围 2、字符长度范围
     */
    @ApiModelProperty(value = "规范检查-字符范围类型")
    public StandardCheckCharRangeTypeEnum standardCheckCharRangeType;

    /**
     * 规范检查-日期格式值，多个日期格式逗号分隔
     */
    @ApiModelProperty(value = "规范检查-日期格式值，多个日期格式逗号分隔")
    public String standardCheckTypeDateValue;

    /**
     * 规范检查-浮点长度范围分隔符
     */
    @ApiModelProperty(value = "规范检查-浮点长度范围分隔符")
    public String standardCheckTypeLengthSeparator;

    /**
     * 规范检查-字符长度范围运算符
     */
    @ApiModelProperty(value = "规范检查-字符长度范围运算符")
    public String standardCheckTypeLengthOperator;

    /**
     * 规范检查-字符长度值/浮点长度范围
     */
    @ApiModelProperty(value = "规范检查-字符长度值/浮点长度范围")
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
     * 是否记录错误数据：0 不记录、1  记录
     */
    @ApiModelProperty(value = "是否记录错误数据：0 不记录、1  记录")
    public int recordErrorData;

    /**
     * 错误数据保留时间：7天、14天、30天
     */
    @ApiModelProperty(value = "错误数据保留时间：7天、14天、30天")
    public int errorDataRetentionTime;

    List<DataCheckEditDTO> dataCheckList;
}
