package com.fisk.datamodel.dto.modelpublish;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class ModelPublishStatusDTO {
    /**
     * 维度id/事实id/宽表id
     */
    @ApiModelProperty(value = "id")
    public int id;
    /**
     * 发布状态
     */
    @ApiModelProperty(value = "发布状态")
    public int status;
    /**
     * 类型：0 DW、1 Doris
     */
    @ApiModelProperty(value = "类型：0 DW、1 Doris")
    public int type;
}
