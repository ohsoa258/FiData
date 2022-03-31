package com.fisk.chartvisual.dto.chartvisual;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author gy
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ReleaseChart extends ChartPropertyDTO {
    public Integer draftId;
    public Long fid;
}
