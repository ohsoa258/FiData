package com.fisk.mdm.dto.masterdata;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 * date 2022/05/06 11:26
 */
@Data
public class ImportParamDTO extends MasterDataBaseDTO {

    @ApiModelProperty(value = "删除空间")
    private Boolean removeSpace;

}
