package com.fisk.datagovernance.entity.dataquality;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import com.fisk.datagovernance.enums.dataquality.*;
import lombok.Data;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author wangjian
 * @date 2024-04-23 15:35:47
 */
@TableName("tb_datacheck_standards_group")
@Data
public class DatacheckStandardsGroupPO extends BasePO {

    @ApiModelProperty(value = "校验组名称")
    private String checkGroupName;

    @ApiModelProperty(value = "数据标准MenuId")
    private Integer standardsMenuId;

    @ApiModelProperty(value = "数据标准id")
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

    @ApiModelProperty(value = "规则执行节点：1、同步前 2、同步中 3、同步后")
    public RuleExecuteNodeTypeEnum ruleExecuteNode;

    @ApiModelProperty(value = "模板id")
    public int templateId;

    @ApiModelProperty(value = "值域检查-类型")
    public RangeCheckTypeEnum rangeCheckType;

    @ApiModelProperty(value = "值域检查-参数类型")
    public int rangeType;

    @ApiModelProperty(value = "值域检查-取值范围类型")
    public RangeCheckValueRangeTypeEnum rangeCheckValueRangeType;

    @ApiModelProperty(value = "值域检查-关键字包含类型")
    public RangeCheckKeywordIncludeTypeEnum rangeCheckKeywordIncludeType;

    @ApiModelProperty(value = "值域检查-取值范围-单向取值-运算符")
    public String rangeCheckOneWayOperator;

    @ApiModelProperty(value = "值域检查-对比值")
    public String rangeCheckValue;

    @ApiModelProperty(value = "规范检查-类型")
    public StandardCheckTypeEnum standardCheckType;

    @ApiModelProperty(value = "规范检查-字符范围类型")
    public StandardCheckCharRangeTypeEnum standardCheckCharRangeType;

    @ApiModelProperty(value = "规范检查-日期格式值，多个日期格式逗号分隔")
    public String standardCheckTypeDateValue;

    @ApiModelProperty(value = "规范检查-浮点长度范围分隔符")
    public String standardCheckTypeLengthSeparator;

    @ApiModelProperty(value = "规范检查-字符长度范围运算符")
    public String standardCheckTypeLengthOperator;

    @ApiModelProperty(value = "规范检查-字符长度值/浮点长度范围")
    public String standardCheckTypeLengthValue;

    @ApiModelProperty(value = "规范检查-正则表达式")
    public String standardCheckTypeRegexpValue;

    @ApiModelProperty(value = "波动检查-类型：SUM、COUNT、AVG、MAX、MIN")
    public FluctuateCheckTypeEnum fluctuateCheckType;

    @ApiModelProperty(value = "波动检查-运算符：>、<、>=、<=、=")
    public String fluctuateCheckOperator;

    @ApiModelProperty(value = "波动检查-波动值")
    public double fluctuateCheckValue;

    @ApiModelProperty(value = "血缘检查-类型")
    public ParentageCheckTypeEnum parentageCheckType;

    /**
     * 正则表达式检查-表达式值
     */
    @ApiModelProperty(value = "正则表达式检查-表达式值")
    public String regexpCheckValue;

    @ApiModelProperty(value = "是否记录错误数据：0 不记录、1  记录")
    public int recordErrorData;

    @ApiModelProperty(value = "错误数据保留时间：7天、14天、30天")
    public int errorDataRetentionTime;
}
