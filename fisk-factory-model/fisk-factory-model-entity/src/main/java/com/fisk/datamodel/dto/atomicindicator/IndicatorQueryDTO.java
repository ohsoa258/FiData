package com.fisk.datamodel.dto.atomicindicator;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class IndicatorQueryDTO {
    @ApiModelProperty(value = "业务区域Id")
    public int businessAreaId;
    @ApiModelProperty(value = "备注")
    public String remark;
    @ApiModelProperty(value = "事实Ids")
    public List<Integer> factIds;
    @ApiModelProperty(value = "宽表Ids")
    public List<Integer> wideTableIds;
}
