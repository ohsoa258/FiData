package com.fisk.datamanagement.dto.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class EntityInstanceDTO {

    @ApiModelProperty(value = "实例guid")
    public String instanceGuid;

}
