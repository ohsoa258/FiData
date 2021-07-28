package com.fisk.chartvisual.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author gy
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ReleaseChart extends ChartPropertyDTO {
    public Integer draftId;
    public Integer folderId;
}
