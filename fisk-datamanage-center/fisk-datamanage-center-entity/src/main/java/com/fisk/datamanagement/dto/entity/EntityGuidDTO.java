package com.fisk.datamanagement.dto.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class EntityGuidDTO {

    @ApiModelProperty(value = "guid")
    public String guid;

    @ApiModelProperty(value = "关联guid")
    public String relationshipGuid;
}