package com.fisk.mdm.dto.stgbatch;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author WangYan
 * @Date 2022/5/9 22:27
 * @Version 1.0
 */
@Data
public class MdmDTO {

    @ApiModelProperty(value = "fidata_id")
    private Integer fidata_id;
    @ApiModelProperty(value = "唯一编码")
    private String code;
}
