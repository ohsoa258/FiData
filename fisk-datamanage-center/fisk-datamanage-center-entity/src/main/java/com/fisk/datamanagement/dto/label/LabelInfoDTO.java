package com.fisk.datamanagement.dto.label;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class LabelInfoDTO {

    @ApiModelProperty(value = "id")
    public Integer id;

    @ApiModelProperty(value = "标签中文名")
    public String labelCnName;

}
