package com.fisk.datamanagement.dto.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class EntityDTO {

    @ApiModelProperty(value = "实体")
    public EntityTypeDTO entity;
}
