package com.fisk.datamanagement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author wangjian
 * @date 2024-03-01 14:35:45
 */
@TableName("tb_business_targetinfo_history")
@Data
public class BusinessTargetinfoHistoryPO extends BasePO {

    @ApiModelProperty(value = "指标分类id")
    private Integer pid;

    @ApiModelProperty(value = "")
    private String historyId;

    @ApiModelProperty(value = "负责部门")
    private String responsibleDept;

    @ApiModelProperty(value = "指标编码")
    private String indicatorCode;

    @ApiModelProperty(value = "指标名称")
    private String indicatorName;

    @ApiModelProperty(value = "指标描述/口径")
    private String indicatorDescription;

    @ApiModelProperty(value = "指标范围")
    private String indicatorLevel;

    @ApiModelProperty(value = "计量单位")
    private String unitMeasurement;

    @ApiModelProperty(value = "统计周期")
    private String statisticalCycle;

    @ApiModelProperty(value = "指标公式")
    private String indicatorformula;

    @ApiModelProperty(value = "指标状态")
    private String indicatorStatus;

    @ApiModelProperty(value = "数据筛选条件")
    private String filteringCriteria;

    @ApiModelProperty(value = "应用")
    private String largeScreenLink;

    @ApiModelProperty(value = "数据粒度")
    private String dataGranularity;

    @ApiModelProperty(value = "营运属性")
    private String operationalAttributes;

    @ApiModelProperty(value = "来源系统")
    private String sourceSystem;

    @ApiModelProperty(value = "来源数据表")
    private String sourceDataTable;

    @ApiModelProperty(value = "指标来源")
    private String sourceIndicators;

    @ApiModelProperty(value = "订单渠道")
    private String orderChannel;

    @ApiModelProperty(value = "指标类型")
    private String indicatorType;

    @ApiModelProperty(value = "数量")
    private String attributesNumber;

    @ApiModelProperty(value = "")
    private String name;

    @ApiModelProperty(value = "")
    private String sqlScript;

    @ApiModelProperty(value = "历史上级指标id")
    public Integer parentBusinessId;

    @ApiModelProperty(value = "历史上级指标名称")
    public String parentBusinessName;

    @ApiModelProperty(value = "历史派生指标")
    public String derivedMetric;
}
