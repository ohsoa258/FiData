package com.fisk.datamanagement.dto.process;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class ProcessUniqueAttributesDTO {

    @ApiModelProperty(value = "限定名称")
    public String qualifiedName;

}
