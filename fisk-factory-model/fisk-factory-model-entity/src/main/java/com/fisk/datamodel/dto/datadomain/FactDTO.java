package com.fisk.datamodel.dto.datadomain;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author WangYan
 * @date 2021/8/16 15:06
 * 事实表 三级
 */
@Data
public class FactDTO {
    @ApiModelProperty(value = "事实Id")
    public Long factId;
    @ApiModelProperty(value = "事实表名")
    public String factTabName;
    /**
     * 原子指标 三级
     */
    @ApiModelProperty(value = "原子性指示器列表")
    public List<AtomicIndicatorsDTO> atomicIndicatorsList;
    /**
     * 派生指标 三级
     */
    @ApiModelProperty(value = "派生指标 三级")
    public List<DerivedIndicatorsDTO> derivedIndicatorsList;

    /**
     * 是否维度 0 否  1 是维度
     */
    @ApiModelProperty(value = "是否维度 0 否  1 是维度")
    public int dimension;
}
