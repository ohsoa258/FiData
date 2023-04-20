package com.fisk.chartvisual.dto.chartvisual;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author WangYan
 * @date 2021/12/1 10:15
 */
@Data
public class IndicatorFeignDTO {

    @ApiModelProperty(value = "指示列表")
    List<IndicatorDTO> indicatorList;
}
