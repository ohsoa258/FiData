package com.fisk.datamanagement.dto.process;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class ProcessDTO {
    @ApiModelProperty(value = "实体")
    public ProcessEntityDTO entity;
}
