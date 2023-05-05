package com.fisk.datamodel.dto.atomicindicator;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class AtomicIndicatorDropListDTO {
    @ApiModelProperty(value = "id")
    public long id;
    /**
     * 原子指标名称
     */
    @ApiModelProperty(value = "指示器名称")
    public String indicatorsName;
}
