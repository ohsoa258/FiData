package com.fisk.chartvisual.dto.chartvisual;

import com.fisk.chartvisual.enums.ChartQueryTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

/**
 * @author gy
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ChartPropertyEditDTO extends ChartPropertyDTO {

    @ApiModelProperty(value = "id")
    @NotNull
    public Integer id;

    @ApiModelProperty(value = "类型")
    public ChartQueryTypeEnum type;

    @ApiModelProperty(value = "fid")
    @NotNull
    public Long fid;
}
