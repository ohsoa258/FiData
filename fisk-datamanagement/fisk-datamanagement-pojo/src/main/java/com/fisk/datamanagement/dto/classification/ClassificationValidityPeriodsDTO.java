package com.fisk.datamanagement.dto.classification;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class ClassificationValidityPeriodsDTO {

    public String startTime;

    public String endTime;
    @ApiModelProperty(value = "时区")
    public String timeZone;

}
