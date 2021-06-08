package com.fisk.chartvisual.dto;

import com.fisk.chartvisual.enums.ChartQueryTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

/**
 * @author gy
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ChartPropertyEditDTO extends ChartPropertyDTO {

    @NotNull
    public Integer id;

    public ChartQueryTypeEnum type;
}
