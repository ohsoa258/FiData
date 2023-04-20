package com.fisk.datamanagement.dto.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class EntityStagingDTO {
    @ApiModelProperty(value = "guid")
    public String guid;
    @ApiModelProperty(value = "name")
    public String name;
    @ApiModelProperty(value = "父类")
    public String parent;
    @ApiModelProperty(value = "类型")
    public String type;
    @ApiModelProperty(value = "展示名字")
    public String displayName;
}
