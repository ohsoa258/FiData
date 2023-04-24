package com.fisk.mdm.dto.masterdata;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class MasterDataDetailsParamDTO extends MasterDataBaseDTO {

    @ApiModelProperty(value = "唯一编码")
    private String code;

}
