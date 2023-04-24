package com.fisk.datamanagement.dto.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class EntityTreeDTO {

    @ApiModelProperty(value = "id")
    public String id;
    @ApiModelProperty(value = "标签")
    public String label;
    @ApiModelProperty(value = "类型")
    public String type;
    @ApiModelProperty(value = "父类ID")
    public String parentId;
    @ApiModelProperty(value = "展示名称")
    public String displayName;
    @ApiModelProperty(value = "子类")
    public List<EntityTreeDTO> children;
}
