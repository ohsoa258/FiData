package com.fisk.mdm.dto.masterdata;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class ImportDataVerifyDTO {

    /**
     * 是否成功
     */
    @ApiModelProperty(value = "是否成功")
    private Boolean success;

    @ApiModelProperty(value = "值")
    private String value;

    @ApiModelProperty(value = "错误信息")
    private String errorMsg;

}
