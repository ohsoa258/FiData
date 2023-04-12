package com.fisk.datagovernance.dto.dataquality.qualityreport;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 质量报告编辑DTO
 * @date 2022/3/24 14:30
 */
@Data
public class QualityReportEditDTO extends QualityReportDTO {
    /**
     * id
     */
    @ApiModelProperty(value = "id")
    public int id;
}
