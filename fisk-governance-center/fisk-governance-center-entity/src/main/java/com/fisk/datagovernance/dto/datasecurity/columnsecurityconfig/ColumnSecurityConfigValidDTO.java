package com.fisk.datagovernance.dto.datasecurity.columnsecurityconfig;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class ColumnSecurityConfigValidDTO {

    @ApiModelProperty(value = "id")
    public long id;

    @ApiModelProperty(value = "有效的")
    public boolean valid;

}
