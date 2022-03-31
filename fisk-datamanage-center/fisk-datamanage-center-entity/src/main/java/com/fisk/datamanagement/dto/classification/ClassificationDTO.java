package com.fisk.datamanagement.dto.classification;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class ClassificationDTO {
    public String typeName;
    @ApiModelProperty(value = "是否传播,默认true")
    public boolean propagate;
    @ApiModelProperty(value = "删除实体时删除传播,默认false")
    public boolean removePropagationsOnEntityDelete;
    public Object attributes;
    @ApiModelProperty(value = "申请有效期")
    public List<ClassificationValidityPeriodsDTO> validityPeriods;
}
