package com.fisk.chartvisual.dto.chartvisual;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author gy
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ReleaseChart extends ChartPropertyDTO {

    @ApiModelProperty(value = "draftId")
    public Integer draftId;

    @ApiModelProperty(value = "fid")
    public Long fid;
}
