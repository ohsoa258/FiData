package com.fisk.datamanagement.dto.metadataentity;

import com.fisk.datamanagement.dto.lineagemaprelation.LineageMapRelationDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author zjy
 * @version 1.0
 * @createTime 2023-03-03 17:23
 * @description
 */
@Data
public class MetadataEntityDTO {
    @ApiModelProperty(value = "id")
    public long id;

    @ApiModelProperty(value = "名称")
    public String name;

    @ApiModelProperty(value = "展示名称")
    public String displayName;

    @ApiModelProperty(value = "拥有者")
    public String owner;

    @ApiModelProperty(value = "描述")
    public String description;

    @ApiModelProperty(value = "类型id")
    public Integer typeId;
    @ApiModelProperty(value = "父类id")
    public Integer parentId;

    @ApiModelProperty(value = "限定名称")
    public String qualifiedName;
    @ApiModelProperty(value = "关系DTO列表")
    public List<LineageMapRelationDTO> relationDTOList;
}
