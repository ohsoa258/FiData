package com.fisk.datamodel.dto.atomicindicator;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class AtomicIndicatorsResultDTO extends AtomicIndicatorsDTO {
    @ApiModelProperty(value = "事实名称")
    public String factName;
    @ApiModelProperty(value = "事实字段名称")
    public String factFieldName;
}
