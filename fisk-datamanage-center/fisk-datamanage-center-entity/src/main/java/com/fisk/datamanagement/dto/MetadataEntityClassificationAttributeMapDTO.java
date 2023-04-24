package com.fisk.datamanagement.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class MetadataEntityClassificationAttributeMapDTO {

    @ApiModelProperty(value = "属性类型Id")
    public Integer attributeTypeId;

    @ApiModelProperty(value = "元数据实体ID")
    public Integer metadataEntityId;

    @ApiModelProperty(value = "值")
    public String value;

    @ApiModelProperty(value = "名称")
    public String name;
    @ApiModelProperty(value = "分类名称")
    public String classificationName;

}
