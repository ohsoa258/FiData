package com.fisk.datamanagement.dto.metadataattribute;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class MetadataAttributeDTO {

    @ApiModelProperty(value = "数据源实体id")
    public Integer metadataEntityId;

    @ApiModelProperty(value = "名称")
    public String name;

    @ApiModelProperty(value = "值")
    public String value;
    /**
     * 0 技术属性 1 元数据属性
     */
    @ApiModelProperty(value = "0 技术属性 1 元数据属性")
    public Integer groupType;

}
