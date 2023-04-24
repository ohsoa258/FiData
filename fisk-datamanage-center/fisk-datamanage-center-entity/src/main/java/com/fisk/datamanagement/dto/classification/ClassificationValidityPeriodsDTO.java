package com.fisk.datamanagement.dto.classification;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class ClassificationValidityPeriodsDTO {

    @ApiModelProperty(value = "开始时间")
    public String startTime;

    @ApiModelProperty(value = "结束时间")
    public String endTime;
    @ApiModelProperty(value = "时区")
    public String timeZone;

}
