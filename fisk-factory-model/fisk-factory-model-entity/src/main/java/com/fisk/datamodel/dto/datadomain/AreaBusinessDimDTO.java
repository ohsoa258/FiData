package com.fisk.datamodel.dto.datadomain;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author WangYan
 * @date 2021/9/10 14:36
 * 业务域  一级
 */
@Data
public class AreaBusinessDimDTO {
    @ApiModelProperty(value = "业务Id")
    public Long businessId;
    @ApiModelProperty(value = "业务名称")
    public String businessName;
    @ApiModelProperty(value = "维度列表")
    public List<DimensionDimDTO> dimensionList;
    @ApiModelProperty(value = "业务进程列表")
    public List<BusinessProcessDimDTO> businessProcessList;
    /**
     *  7.业务域  8.维度  9.业务过程  10.事实表
     */
    @ApiModelProperty(value = "标记 7.业务域  8.维度  9.业务过程  10.事实表")
    public Integer flag;
}
