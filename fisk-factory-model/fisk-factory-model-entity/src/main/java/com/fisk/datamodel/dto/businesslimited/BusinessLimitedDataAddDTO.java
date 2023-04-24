package com.fisk.datamodel.dto.businesslimited;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class BusinessLimitedDataAddDTO extends BusinessLimitedUpdateDTO {

    @ApiModelProperty(value = "事实Id")
    public int factId;

}
