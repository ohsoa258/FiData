package com.fisk.mdm.dto.mathingrules;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JinXingWang
 */
@Data
public class SourceSystemFiledMappingDto {
    @ApiModelProperty(value = "源系统字段ID")
    public Integer sourceSystemFiledId;
    @ApiModelProperty(value = "属性ID")
    public Integer attitudeId;
}
