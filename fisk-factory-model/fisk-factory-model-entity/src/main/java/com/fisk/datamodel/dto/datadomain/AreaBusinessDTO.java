package com.fisk.datamodel.dto.datadomain;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author WangYan
 * @date 2021/8/19 10:16
 * 一级业务域
 */
@Data
public class AreaBusinessDTO {
    @ApiModelProperty(value = "业务Id")
    public Long businessId;
    @ApiModelProperty(value = "业务名称")
    public String businessName;
    @ApiModelProperty(value = "维度列表")
    public List<DimensionDTO> dimensionList;
    @ApiModelProperty(value = "业务进程列表")
    public List<BusinessProcessDTO> businessProcessList;
}
