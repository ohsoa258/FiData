package com.fisk.chartvisual.dto.contentsplit;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author WangYan
 * @date 2021/10/28 17:23
 */
@Data
public class ChinaMapDTO {

    @ApiModelProperty(value = "id")
    public Integer id;

    @ApiModelProperty(value = "名称")
    public String name;

    @ApiModelProperty(value = "值")
    public String value;
}
